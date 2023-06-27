#include "utils-common.hpp"
#include "utils-party.hpp"
#include "xgboost.hpp"

void random_data()
{
	string indatafilepath = "../../data/Credit/UCI_Credit_Card.csv";
	ifstream filein(indatafilepath);
	string line;
	getline(filein, line);
	string labelline = line;
	if(!filein)
	{
		cout << "No such files!" << endl;
		return;
	}
	vector<string> datas;
	while(getline(filein, line))
	{
		if(line == "")
		{
			break;
		}
		datas.push_back(line);
	}
	random_device rd;
    mt19937 g(rd());
    shuffle(datas.begin(), datas.end(), g);
	vector<string> train_datas(datas.begin(), datas.begin()+24000);
	vector<string> test_datas(datas.begin()+24001, datas.end());

	string out_train_data_path = "../../data/Credit/random_dataset/train_data.csv";
	string out_test_data_path = "../../data/Credit/random_dataset/test_data.csv";
	ofstream outfile_train(out_train_data_path);
	outfile_train << labelline << endl;
	int id = 0;
	for(auto l : train_datas)
	{
		size_t pos = l.find(',');
		string new_l = to_string(id)+","+l.substr(pos+1);
		outfile_train << new_l << endl;
		id++;
	}
	ofstream outfile_test(out_test_data_path);
	outfile_test << labelline << endl;
	id = 0;
	for(auto l : test_datas)
	{
		size_t pos = l.find(',');
		string new_l = to_string(id)+","+l.substr(pos+1);
		outfile_test << new_l << endl;
		id++;
	}
	outfile_train.close();
	outfile_test.close();
	filein.close();

	ifstream infile_train(out_train_data_path);
	getline(infile_train, line);
	if(!infile_train)
	{
		cout << "No such files!" << endl;
		return;
	}
	vector<Features> all_features = 
	{ID,LIMIT_BAL,SEX,EDUCATION,MARRIAGE,AGE,
	PAY_1,PAY_2,PAY_3,PAY_4,PAY_5,PAY_6,
	BILL_AMT1,BILL_AMT2,BILL_AMT3,BILL_AMT4,BILL_AMT5,BILL_AMT6,
	PAY_AMT1,PAY_AMT2,PAY_AMT3,PAY_AMT4,PAY_AMT5,PAY_AMT6,LABEL};
	unordered_map<Features, vector<int>> train_data;
	while(getline(infile_train, line))
	{
		vector<string> elements;
		Stringsplit(line, ',', elements);
		for(int it = 0; it < all_features.size(); it++)
		{
			train_data[all_features[it]].push_back(atoi(elements[it].c_str()));
		}
	}
	
	for(auto p : client)
	{
		ofstream fileout_party(paths[p]);
		fileout_party << attributes[p] << endl;
		for(int i = 0; i < train_data[ID].size(); i++)
		{
			for(auto fea : individual_features[p])
			{
				fileout_party << train_data[fea][i] << ",";
			}
			fileout_party << endl;
		}
	}


}

int main(int argc, char **argv)
{
	// random_data();
	// return 0;
	timer.record();
	int r = 0;
	
	unordered_map<Entity, MyParty> parties;
	for(auto p : client)
	{
		parties[p] = MyParty(p);
	}
	XGBoost booster;
	string sep("-");
	while(r < config.max_tree_num)
	{
		r++;
		if(r != 1)
		{
			booster.clear();
			for(auto p : client)
			{
				parties[p].clear_tree();
			}
		}
		
		GradAndHess_list GH_list = booster.CalculateGradHess(r, parties[acp].data[LABEL]);
		int t = 0;
		while(t < booster.max_node_id)
		{
			
			t++;
			if(booster.tree[t/2].is_leaf == true)
			{
				booster.construct_tree(r, t);
				continue;
			}

			cout << "=============================== Constructing the "<<to_string(t) <<"-th node of "<< to_string(r) << "-th tree ===============================" << endl;
			if(t >= booster.min_leaf_node_id)
			{
				bool hasTrue = any_of(parties[acp].node_samples_onehot[t].begin(), parties[acp].node_samples_onehot[t].end(), [](bool b) { return b; });
				if(hasTrue)
					booster.construct_leafnode(r, t, parties[acp].node_samples_onehot[t]);
				booster.construct_tree(r, t);
				continue;
			}
			for(auto p : client)
			{
				parties[p].Split_feature_buckets_for_node(t);
				parties[p].mul_GL_BM(t, GH_list);
				booster.merge_Lsum(t, parties[p].GH_split_sum[t]);
			}
			
			bool is_leaf = false;

			string best_split = booster.Find_best_split(t, is_leaf);
			cout << best_split << " : ";
			
			// is_leaf = parties[p_star].is_leaf_node(t, d_star, i_star);
			// cout << "is_leaf:" << to_string(is_leaf) << endl;                                                     
                               
			if(!is_leaf)
			{
				size_t pos = best_split.find('-');
				Entity p_star = Entity(atoi(best_split.substr(0, pos).c_str()));
				best_split = best_split.substr(pos+1);
				pos = best_split.find('-');
				Features d_star = Features(atoi(best_split.substr(0,pos).c_str()));
				best_split = best_split.substr(pos+1);
				int i_star = atoi(best_split.c_str());
				cout << parties[p_star].threshold_sets[d_star][i_star] << endl;

				int threshold = parties[p_star].Feedback_threshold(t, d_star, i_star);
				for(auto p : client)
				{
					if(p == p_star) continue;
					parties[p].update_samples_threshold(t, parties[p_star].node_samples_onehot);
				}
				booster.construct_splitnode(r, t, p_star, d_star, threshold);
				booster.construct_tree(r,t);
			}
			else
			{
				booster.construct_leafnode(r, t, parties[acp].node_samples_onehot[t]);
				booster.construct_tree(r, t);
			}
			// parties[acp].clear_GL_sum();

		}
		booster.Jsondump_xgboost();
		// parties[acp].show_situation();
	}
	cout << "训练完成";
	timer.show();



	//refresh test data
	string test_data_path("../../data/Credit/random_dataset/test_data.csv");
	// ifstream infile(test_data_path); // 打开csv文件
	// ofstream outfile(test_data_path.substr(0, test_data_path.length()-4)+"_refresh"+test_data_path.substr(test_data_path.length()-4));
	// string line;
	// getline(infile, line);
	// outfile << line << endl;
	// int id = 1;
	// string post_text;
	// while (getline(infile, line))
	// {
	// 	post_text = line.substr(line.find(","));
	// 	outfile << id++ << post_text << endl;
	// }
	// infile.close(); // 关闭文件
	// outfile.close(); // 关闭文件
	// test_data_path = test_data_path.substr(0, test_data_path.length()-4)+"_refresh"+test_data_path.substr(test_data_path.length()-4);

	//read test data
	ifstream filein(test_data_path);
	if(!filein)
	{
		cout << "No such files!" << endl;
		return 0;
	}
	string lines;
	getline(filein, lines);
	
	vector<Features> all_features = 
	{ID,LIMIT_BAL,SEX,EDUCATION,MARRIAGE,AGE,
	PAY_1,PAY_2,PAY_3,PAY_4,PAY_5,PAY_6,
	BILL_AMT1,BILL_AMT2,BILL_AMT3,BILL_AMT4,BILL_AMT5,BILL_AMT6,
	PAY_AMT1,PAY_AMT2,PAY_AMT3,PAY_AMT4,PAY_AMT5,PAY_AMT6,LABEL};
	unordered_map<Features, vector<int>> test_data;
	while(getline(filein, lines))
	{
		if(lines == "")
		{
			break;
		}
		vector<string> elements;
		Stringsplit(lines, ',', elements);
		for(int it = 0; it < all_features.size(); it++)
		{
			test_data[all_features[it]].push_back(atoi(elements[it].c_str()));
		}
	}
	cout << "Read test data already!" << endl;

	//predict
	timer.record();
	unordered_map<int, int> prediction = booster.predict(test_data);
	cout << "查询完成";
	timer.show();
	int count_right = 0;
	int TP = 0, TN = 0, FP = 0, FN = 0;
	for(auto pred : prediction)
	{
		int tar = test_data[LABEL][pred.first];
		int pre = pred.second;
		// cout << pre << " ";
		if(tar == 1)
		{
			if(pre == 1)
			{
				TP+=1;
				continue;
			}
			else
			{
				FN += 1;
				continue;
			}
		}
		else
		{
			if(pre == 1)
			{
				FP += 1;
				continue;
			}
			else
			{
				TN += 1;
			}
		}
	}
	cout << TP << " " << TN << " " << FP << " " << FN << endl;
	cout << "ACC:" << (double)(TP+TN) / (TP+TN+FP+FN) << endl;
	double precision = (double) TP / (double)(TP+FP);
	double recall = (double) TP / (double)(TP+FN);
	cout << "F1:" << (double)(2*precision*recall) / (double)(precision + recall) << endl;

	return 0;
}