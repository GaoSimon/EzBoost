#ifndef PARTY_H
#define PARTY_H

#include "utils_common.hpp"



class MyParty {
public:
	Entity k;
	int dim_num;
	unordered_map<int, unordered_map<Features, int>> threshold_number; //前一个int是第t个节点建立时的阈值数量
	unordered_map<Features, vector<int>> threshold_sets; //第一个int是表示第t个节点
	string datafile_path;
	unordered_map<Features, vector<int>> data;

	vector<Features> my_features;
	
	unordered_map<Features, Mymat> fea_bins;

	unordered_map<int, Myvec> node_samples_onehot;//第一个int是表示第t个节点,vector<bool>是该节点中第i个样本是否存在
	map<int, map<string, GradAndHess>> GradAndHess_Lsum; //第一个int是表示第t个节点
	string query_comparison;
	unordered_map<int, vector<bool>> tree_comparisons;
	
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
			vector<string> elements = SplitString(line, ',');
			for(int it = 0; it < my_features.size(); it++)
			{
				data[my_features[it]].push_back(atoi(elements[it].c_str()));
			}
		}
		assert(config.samples_num == data[ID].size());
		node_samples_onehot[1].resize(1, config.samples_num);
		node_samples_onehot[1].setConstant(true);
		cout << "数据读取完毕!" << endl;

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
		
		string str_BM_a = to_string(BMtype)+sep+to_string(k)+bline,str_BM_b = to_string(BMtype)+sep+to_string(k)+bline;
		for(auto d : my_features)
		{	
			if(d == ID || d == LABEL)
				continue;
			pair<Mymat,Mymat> BM_share = Share_Bucket_Matrix(d);
			pair<string, string> str_BM_share = JsonDump_Bucket_Matrix(BM_share);
			str_BM_a += to_string(d)+sep+str_BM_share.first+"$";
			str_BM_b += to_string(d)+sep+str_BM_share.second+"$";
		}
		zmq_send_msg(str_BM_a, S1);
		zmq_send_msg(str_BM_b, S2);
		
		cout << "参与方" << to_string(k) << "已经将数据桶秘密共享份额分别发送给S1和S2! " << endl;

	}


	pair<Mymat,Mymat> Share_Bucket_Matrix(Features d)
	{
		Mymat bktmat = fea_bins[d];
		// srand(time(nullptr));
		Mymat bktmat_alpha(bktmat.rows(), config.samples_num);
		bktmat_alpha.setRandom();
		Mymat bktmat_beta(bktmat.rows(), config.samples_num);
		bktmat_beta = bktmat ^ bktmat_alpha;
		return make_pair(bktmat_alpha,bktmat_beta);
	}

	void Store_GradHess_sum(int& tag, int t, string str_gradient_sum)
	{
		vector<string> str_row_gradients_sum_share = SplitString(str_gradient_sum, '\n');
		for(auto row : str_row_gradients_sum_share)
		{
			size_t pos = row.find('$');
			string ident = row.substr(0,pos);
			row = row.substr(pos+1);
			pos = row.find('-');
			uint64_t grad = strtoull(row.substr(0,pos).c_str(), NULL, 10) & mask;
			uint64_t hess = strtoull(row.substr(pos+1).c_str(), NULL, 10) & mask;
			if(tag == 0)
			{
				GradAndHess_Lsum[t][ident] = GradAndHess{grad,hess};
			}
			else
			{
				grad = (GradAndHess_Lsum[t][ident].grad + grad) & mask;
				// cout << "recover: " << grad << endl;
				hess = (GradAndHess_Lsum[t][ident].hess + hess) & mask;
				GradAndHess_Lsum[t][ident] = GradAndHess{grad,hess};
			}
		}
		tag++;
	}

	void Mul_BM_GH(int node_id, GradAndHess_list GH_list)
	{
		// timer.record();
		for(auto d : my_features)
		{
			if(d == ID || d == LABEL)
				continue;
			auto BM = fea_bins[d];
			for(int i = 0; i < BM.rows(); i++)
			{
				BM.row(i) = BM.row(i) && node_samples_onehot[node_id];
			}
			for(int i = 0; i < BM.rows(); i++)
			{
				uint64_t grad_sum = 0, hess_sum = 0;
				for(int j = 0; j < config.samples_num; j++)
				{
					if(BM(i,j))
					{
						grad_sum = (uint64_t)((int)grad_sum + (int)GH_list[j].grad);
						hess_sum = (uint64_t)((int)hess_sum + (int)GH_list[j].hess);
					}
				}
				GradAndHess_Lsum[node_id][to_string(acp)+sep+to_string(d)+sep+to_string(i)] = GradAndHess{grad_sum, hess_sum};
			}
		}
		cout << "主动方计算本地乘积完成！" << endl;
		// timer.show();
	}

	void Store_Comparisons(int& tag, string str_comparisons)
	{
		if(tag == 1)
		{
			query_comparison = str_comparisons;
			tag++;
		}
		else if(tag == 2)
		{
			vector<string> str_res_mills_a = SplitString(query_comparison, '\n');
			vector<string> str_res_mills_b = SplitString(str_comparisons, '\n');
			for(int i = 0; i < str_res_mills_a.size(); i++)
			{
				vector<string> str_res_a = SplitString(str_res_mills_a[i], '-');
				vector<string> str_res_b = SplitString(str_res_mills_b[i], '-');
				int tree_id = atoi(str_res_a[0].c_str());
				vector<bool> comparisons;
				for(int i = 1; i < str_res_a.size(); i++)
				{
					comparisons.push_back((bool)atoi(str_res_a[i].c_str()) ^ (bool)atoi(str_res_b[i].c_str()));
				}
				tree_comparisons[tree_id] = comparisons;
				tag++;
			}
			
		}
	}

	bool is_leaf_node(int t, Features d, int i)
	{
		cout << i << " " << threshold_number[t][d] << endl;
		if(i+1 == threshold_number[t][d])
			return true;
		return false;
	}
	void show_onehot(vector<bool> vec)
	{
		for(auto v : vec)
		{
			cout << v ;
		}
		cout << endl;
	}
	pair<int, int> Share_thresholds(int t, Features d, int i )
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
		int threshold_a = int(rand());
		int threshold_b = threshold - threshold_a;
		return make_pair(threshold_a, threshold_b);
	}

	void update_samples_onehot(int t, unordered_map<int, Myvec> node_samples)
	{
		node_samples_onehot[2*t] = node_samples[2*t];
		node_samples_onehot[2*t+1] = node_samples[2*t+1];
	}
	void clear()
	{
		node_samples_onehot.clear();
		node_samples_onehot[1].resize(1, config.samples_num);
		node_samples_onehot[1].setConstant(true);
	}
};



#endif
