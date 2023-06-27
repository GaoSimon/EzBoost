#ifndef PARTY_H
#define PARTY_H

#include "utils-common.hpp"



class MyParty {
public:
	Entity k;

	unordered_map<Features, vector<int>> threshold_sets; //第一个int是表示第t个节点
	string datafile_path;
	unordered_map<Features, vector<int>> data;

	vector<Features> my_features;
	unordered_map<Features, Mymat> fea_bins;
	unordered_map<int, unordered_map<Features, Mymat>> node_fea_M;
	unordered_map<int, Myvec> node_samples_onehot;//第一个int是表示第t个节点,vector<int>是该节点的样本id集合
	unordered_map<int, unordered_map<string, GradAndHess>> GH_split_sum;//第一个int是表示第t个节点

	
public:
	MyParty(){}
	MyParty(Entity k_in)
	{
		k = k_in;
		datafile_path = paths[k];
		my_features = individual_features[k];
		Refresh_data();
		Read_data();
	}
	~MyParty(){}

	void Refresh_data()
	{
		ifstream infile(datafile_path); // 打开csv文件
		ofstream outfile(datafile_path.substr(0, datafile_path.length()-4)+"_refresh"+datafile_path.substr(datafile_path.length()-4));
		string line;
		getline(infile, line);
		outfile << line << endl;
		int id = 0;
		string post_text;
		while (getline(infile, line))
		{
			post_text = line.substr(line.find(","));
			outfile << id++ << post_text << endl;
		}
		infile.close(); // 关闭文件
		outfile.close(); // 关闭文件
		datafile_path = datafile_path.substr(0, datafile_path.length()-4)+"_refresh"+datafile_path.substr(datafile_path.length()-4);
	}

	void Read_data() 
	{
		ifstream filein(datafile_path);
		string line;
		getline(filein, line);
		if(!filein)
		{
			cout << "No such files!" << endl;
			return;
		}

		while(getline(filein, line))
		{
			if(line == "")
			{
				break;
			}
			vector<string> elements;
			Stringsplit(line, ',', elements);
			for(int it = 0; it < my_features.size(); it++)
			{
				data[my_features[it]].push_back(atoi(elements[it].c_str()));
			}
		}
		
		assert(config.samples_num == data[ID].size());
		node_samples_onehot[1].resize(1, config.samples_num);
		node_samples_onehot[1].setConstant(true);
		cout << "Read data already!" << endl;

		for(Features d : my_features)
		{
			if (d == ID || d == LABEL)
				continue;
			Mymat  BktMat;
			
			vector<pair<int,int>> f_val; //该特征的值列表
			for(int i = 0; i < config.samples_num; i++)
			{
				f_val.push_back(make_pair(data[d][i], i));
			}
			sort(f_val.begin(), f_val.end());
			set<int> s(data[d].begin(), data[d].end());
			vector<int> d_val;
			d_val.assign(s.begin(), s.end());
			if(d_val.size() <= config.init_threshold_num)
			{
				BktMat.resize(d_val.size(), config.samples_num);
				BktMat.setConstant(false);
				auto it = f_val.begin();
				int threshold;
				for(int i = 0; i < d_val.size(); i++)
				{
					threshold = d_val[i];
					threshold_sets[d].push_back(threshold);
					while(it != f_val.end() && it->first <= threshold)
					{
						BktMat(i, it->second) = true;
						it++;
					}
				}
			}
			else
			{
				BktMat.resize(config.init_threshold_num, config.samples_num);
				BktMat.setConstant(false);
				int gap = d_val.size() / config.init_threshold_num;
				auto it = f_val.begin();
				int threshold;
				for(int i = 0; i < config.init_threshold_num-1; i++)
				{
					threshold = d_val[gap*(i+1)-1];
					threshold_sets[d].push_back(threshold);
					while(it != f_val.end() && it->first <= threshold)
					{
						BktMat(i, it->second) = true;
						it++;
					}
				}
				threshold = d_val.back();
				threshold_sets[d].push_back(threshold);
				while(it != f_val.end())
				{
					BktMat(config.init_threshold_num-1, it->second) = true;
					it++;
				}
			}
			fea_bins[d] = BktMat;
		}
		cout << "数据桶划分完毕！" << endl;

	}

	void Show_data(int num=0)
	{
		for(auto it : my_features)
		{
			cout << it << "\t" << endl;
		}
		if(num == 0) num = data[ID].size();
		
		for(int i = 0; i < num; i++)
		{	
			for(auto it : my_features)
			{
				cout << data[it][i] << "\t" << endl;
			}
		}	
	}

	void Split_feature_buckets_for_node(int node_id)
	{	
		if(node_id == 1)
		{
			node_fea_M[node_id] = fea_bins;
			return;
		}
		for(Features d : my_features)
		{
			if (d == ID || d == LABEL)
				continue;
			node_fea_M[node_id][d].resize(fea_bins[d].rows(), config.samples_num);
			for(int i = 0; i < fea_bins[d].rows(); i++)
			{
				node_fea_M[node_id][d].row(i) = fea_bins[d].row(i) && node_samples_onehot[node_id];
			}
		}
	}

	void mul_GL_BM(int node_id, GradAndHess_list GH_list)
	{
		for(auto d : my_features)
		{
			if(d == ID || d == LABEL)
				continue;
			auto BM = node_fea_M[node_id][d];
			for(int i = 0; i < BM.rows(); i++)
			{
				double grad_sum = 0, hess_sum = 0;
				for(int j = 0; j < config.samples_num; j++)
				{
					if(BM(i,j))
					{
						grad_sum += GH_list[j].grad;
						hess_sum += GH_list[j].hess;
					}
				}
				string ident = to_string(k)+"-"+to_string(d)+"-"+to_string(i);
				GH_split_sum[node_id][ident] = GradAndHess{grad_sum, hess_sum};
			}
		}
	}

	// bool is_leaf_node(int t, Features d, int i)
	// {
	// 	cout << i << " " << threshold_number[t][d] << endl;
	// 	if(i+1 == threshold_number[t][d])
	// 		return true;
	// 	return false;
	// }
	int Feedback_threshold(int t, Features d, int i )
	{
		int threshold = threshold_sets[d][i];
		node_samples_onehot[2*t].resize(1, config.samples_num);
		node_samples_onehot[2*t+1].resize(1, config.samples_num);
		for(int id = 0; id < config.samples_num; id++)
		{
			if(node_samples_onehot[t](0,id))
			{
				if(data[d][id] <= threshold)
				{
					node_samples_onehot[2*t](0,id) = true;
					node_samples_onehot[2*t+1](0,id) = false;
				}
				else
				{
					node_samples_onehot[2*t](0,id) = false;
					node_samples_onehot[2*t+1](0,id) = true;
				}
			}
			else
			{
				node_samples_onehot[2*t](0,id) = false;
				node_samples_onehot[2*t+1](0,id) = false;
			}
		}
		
		return threshold;
	}

	void update_samples_threshold(int t, unordered_map<int, Myvec> node_samples)
	{
		node_samples_onehot[2*t] = node_samples[2*t];
		node_samples_onehot[2*t+1] = node_samples[2*t+1];
	}

	void clear_GL_sum()
	{
		GH_split_sum.clear();
	}

	void clear_tree()
	{
		node_samples_onehot.clear();
		node_samples_onehot[1].resize(1, config.samples_num);
		node_samples_onehot[1].setConstant(true);
		node_fea_M.clear();
		GH_split_sum.clear();
	}
	// void show_situation()
	// {
	// 	cout << "Entity: " << k << endl;
	// 	cout << "Node_samples_id" << endl;
	// 	for(auto samples : node_samples_id)
	// 	{
	// 		cout << "Node " << samples.first << ": {";
	// 		for(auto id : samples.second)
	// 		{
	// 			cout << id << ",";
	// 		}
	// 		cout << "}" << endl;
	// 	}
	// 	cout << endl;
	// 	// cout << "Threshold" << endl;
	// 	// for(auto node_thresholds : threshold_sets)
	// 	// {
	// 	// 	cout << "Node " << node_thresholds.first << ": {\n";
	// 	// 	for(auto fea_thresholds : node_thresholds.second)
	// 	// 	{
	// 	// 		cout << "Feature " << fea_thresholds.first << ":{";
	// 	// 		for(auto threshold : fea_thresholds.second)
	// 	// 		{
	// 	// 			cout << threshold << ",";
	// 	// 		}
	// 	// 		cout << "}" << endl;
	// 	// 	}
	// 	// 	cout << "}" << endl;
	// 	// }
	// 	// cout << endl;
	// 	// cout << "Gradients_Sum" << endl;
	// 	// for(auto node_gradient_sum : gradients_Lsum)
	// 	// {
	// 	// 	cout << "Node " << node_gradient_sum.first << ": {\n";
	// 	// 	for(auto ident_gradient_sum : node_gradient_sum.second)
	// 	// 	{
	// 	// 		cout << "ident " << ident_gradient_sum.first << ":{";
	// 	// 		cout << (int)ident_gradient_sum.second.grad << ", " << (int)ident_gradient_sum.second.hess;
	// 	// 		cout << "}" << endl;
	// 	// 	}
	// 	// 	cout << "}" << endl;
	// 	// }
	// }
};



#endif
