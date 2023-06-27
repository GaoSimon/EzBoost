#ifndef XGBOOST_H
#define XGBOOST_H

#include "utils_common.hpp"
#define magnify 1000
#define roundsize 3


class XGBoost {
public:
	struct APNode {
		bool is_leaf;
		Entity split_Entity;
		Features split_feature;
		int split_threshold_id;
		double leaf_value;
	};
	typedef unordered_map<int, APNode> APTree;
	APTree tree;
	unordered_map<int, APTree> boost;
	unordered_map<int, double> gsum, hsum;
	double lambda = 1;
	double y_pred;
	int min_leaf_node_id;
	int max_node_id;
	unordered_map<int, unordered_map<int, int>> id_in_leafnode;//第一个int是树id,第二个int是样本id，第三个int是叶节点id
	GradAndHess_list gradhess_list;
public: 
	XGBoost(){
		min_leaf_node_id = pow(2, config.max_depth-1);
		max_node_id = pow(2, config.max_depth) - 1;
		gsum[1] = 0, hsum[1] = 0;
	}
	~XGBoost(){}

	void CalculateGradHess(int tree_id, vector<int> labels)
	{
		GradAndHess gradient{};
		if(tree_id == 1)
		{			
			double sum = 0;
			for(auto label : labels)
			{
				sum += label;
			}
			double mean = sum / (double)labels.size();
			y_pred = 0.5 * log((1 + mean) / (1 - mean));
			for(auto y : labels)
			{
				double pred = 1.0 / (1.0 + exp(-y_pred));
				double grad = (-y + (1 - y) * exp(pred)) / (1 + exp(pred));
				double round_grad = grad = round_double(grad, roundsize);
				double hess = exp(pred) / pow((1 + exp(pred)), 2);
				double round_hess = round_double(hess, roundsize);
				gsum[1] += round_grad, hsum[1] += round_hess;
				int int_grad = round_grad*magnify, int_hess = round_hess * magnify;
				gradient = {uint64_t(int_grad), uint64_t(int_hess)};
				// cout << (int) gradient.grad << endl;
				gradhess_list.push_back(gradient);
			}

		}
		else
		{
			for(int i = 0; i < labels.size(); i++)
			{
				int y = labels[i];
				y_pred = 0;
				for(int tree_i = 1; tree_i < tree_id; tree_i++)
				{
					int node_id = id_in_leafnode[tree_i][i];
					y_pred += boost[tree_i][node_id].leaf_value;
				}
				double pred = 1.0 / (1.0 + exp(-y_pred));
				double grad = (-y + (1 - y) * exp(pred)) / (1 + exp(pred));
				double round_grad = round_double(grad, roundsize);
				double hess = exp(pred) / pow((1 + exp(pred)), 2);
				double round_hess = round_double(hess, roundsize);
				gsum[1] += round_grad, hsum[1] += round_hess;
				int int_grad = round_grad*magnify, int_hess = round_hess * magnify;
				gradient = {uint64_t(int_grad), uint64_t(int_hess)};
				gradhess_list.push_back(gradient);

			}
		}
		cout << "主动方计算第" << tree_id << "棵树的梯度完成！" << endl;
	};

	void Share_Gradients_list(GradAndHess_list raw_gradients, GradAndHess_list &GradAndHess_alpha, GradAndHess_list & GradAndHess_beta)
	{
		for(auto gradient : raw_gradients)
		{
			uint64_t grad = gradient.grad;
			uint64_t hess = gradient.hess;
			// srand(time(NULL));
			uint64_t grad_a = uint64_t(rand() % magnify);
			uint64_t hess_a = uint64_t(rand() % magnify);
			uint64_t grad_b = grad - grad_a;
			uint64_t hess_b = hess - hess_a;
			GradAndHess gradient_a={grad_a, hess_a}, gradient_b={grad_b, hess_b};
			GradAndHess_alpha.push_back(gradient_a);
			GradAndHess_beta.push_back(gradient_b);
		}
	}

	string Find_best_split(int node_id, map<string, GradAndHess> GH_Lsum, bool & is_leaf)
	{
		double score = -100000;
		string best_key("");
		
		for(Entity p : client)
		{
			for(Features d : individual_features[p])
			{
				if(d == ID || d == LABEL)
					continue;
				double gL = 0, hL = 0;
				for(int i = 0; i < config.init_threshold_num; i++)
				{
					string ident = to_string(p)+sep+to_string(d)+sep+to_string(i);
					
					gL += (double)((int)(GH_Lsum[ident].grad)) / (double)magnify;
					hL += (double)((int)(GH_Lsum[ident].hess)) / (double)magnify;
					double gR = gsum[node_id]-gL;
					double hR = hsum[node_id]-hL;
					double gain = pow(gL, 2) / (hL + lambda) + pow(gR, 2) / (hR + lambda) - pow(gsum[node_id],2) / (hsum[node_id]+lambda);
					if(gain-score> 0.0001)
					{
						score = gain;
						best_key = ident;
						if(gR == 0 )
						{
							is_leaf = true;
						}
						else if(gR != 0 )
						{
							is_leaf = false;
						}
						gsum[2*node_id] = gL, hsum[2*node_id] = hL;
						gsum[2*node_id+1] = gR, hsum[2*node_id+1] = hR;
					}
				}
			}
		}
		if(best_key == "")
		{
			cout << "cannot find best split." << endl;
		}
		if(score <= 0)
		{
			is_leaf = true;
		}
		// cout << "最佳划分增益值为" <<score  << endl;
		return best_key;
	}

	void construct_splitnode(int r, int node_id, Entity en, Features fea, int threshold_id)
	{
		APNode node{false, en, fea, threshold_id, 0};
		tree[node_id] = node;
		cout << "=============== 第 " << to_string(r) << "棵树的第" << to_string(node_id) << "个节点构建完成 ===============" << endl;
		construct_tree(r, node_id);
	}

	void construct_leafnode(int r, int node_id, Myvec id_samples)
	{
		APNode node{true, Noen, Nofea, -1, 0};

		node.leaf_value = (double) - gsum[node_id] / (hsum[node_id] + lambda);
		cout << "leaf_value: " << node.leaf_value << endl; 
		for(int id = 0; id < config.samples_num; id++)
		{
			if(id_samples(0, id))
				id_in_leafnode[r][id] = node_id;
		}
		tree[node_id] = node;
		cout << "=============== 第 " << to_string(r) << "棵树的第" << to_string(node_id) << "个节点构建完成 ===============" << endl;
		
	}
	
	void construct_tree(int r, int node_id)
	{
		if(node_id == max_node_id)
		{
			boost[r] = tree;
			tree.clear();
			cout << "============================== 第" << to_string(r) << "棵树构建完成 ==============================" << endl;
		}
	}

	string Jsondump_xgboost()
	{
		string str_xgboost;
		for(auto xtree : boost)
		{
			string str_tree(to_string(xtree.first)+"_th tree:\n");
			for(auto node : xtree.second)
			{
				str_tree += to_string(node.first)+": {";
				str_tree += "is_leaf:" + to_string(node.second.is_leaf) + ",";
				str_tree += "split_Entity:" + to_string(node.second.split_Entity) + ",";
				str_tree += "split_feature:" + to_string(node.second.split_feature) + ",";
				str_tree += "split_threshold_id:" + to_string(node.second.split_threshold_id) + ",";
				str_tree += "leaf_value:" + to_string(node.second.leaf_value) + "}\n";
			}
			str_xgboost += str_tree;
		}
		cout << str_xgboost << endl;
		return str_xgboost;

	}

	void save_xgboost(string filepath)
	{
		filesystem::create_directories(filesystem::path(filepath).parent_path());
		string str_xgboost =  Jsondump_xgboost();
		ofstream myfile(filepath);    

		if (myfile.is_open()) {  
			myfile << str_xgboost;  
			myfile.close();      
		} else {
			cout << "Unable to open file"; 
		}
	}

	void Jsonload_xgboost(string filepath)
	{
		vector<string> lines;
		ifstream file(filepath);
		int tree_id, node_id;
		if (file.is_open()) {
			string line;
			while (getline(file, line)) {
				stringstream ss(line);
				vector<double> nums;

				char c;
				while (ss >> c) {
					if (isdigit(c) || c == '.' || c == '-') {
						string num_str;
						num_str += c;
						while (ss >> c && (isdigit(c) || c == '.')) {
							num_str += c;
						}
						double num = stod(num_str);
						nums.push_back(num);
					}
				}
				if(nums.size() == 1)
				{
					tree_id = int(nums[0]);
				}
				else
				{
					node_id = int(nums[0]);
					boost[tree_id][node_id].is_leaf = (bool)(int)nums[1];
					boost[tree_id][node_id].split_Entity = (Entity)(int)nums[2];
					boost[tree_id][node_id].split_feature = (Features)(int)nums[3];
					boost[tree_id][node_id].split_threshold_id = (int)nums[4];
					boost[tree_id][node_id].leaf_value = nums[5];
				}

			}
			file.close();
		}
	}

	void clear()
	{
		gsum.clear();
		hsum.clear();
		gradhess_list.clear();
	}
};

class SVBoost {
public:
	struct svnode
	{
		Features feature;
		int threshold;
	};
	typedef unordered_map<int, svnode> svtree;
	svtree tree;
	unordered_map<int, svtree> boost;
	unordered_map<int, vector<int>> node_id_spilt_to; //前一个int是指树的id，后一个int是指当前查询需要比对的节点id
	unordered_map<Features, vector<int>> row_data;
public:
	SVBoost(){
	}
	~SVBoost(){}
	void construct_node(int t, Features fea, int threshold_share )
	{
		tree[t].feature = fea;
		tree[t].threshold = threshold_share;
	}
	void construct_tree(int r)
	{
		boost[r] = tree;
		tree.clear();
		for(int i = 0; i < config.query_num; i++)
		{
			node_id_spilt_to[r].push_back(1); //一开始节点id是1也就是根节点
		}
		
	}
	void update_query_node_id(string str)
	{
		vector<string> str_tree_split_node_s = SplitString(str, '\n');
		for(auto str_tree_split_node : str_tree_split_node_s)
		{
			vector<string> tree_split_node = SplitString(str_tree_split_node, ',');
			int tree_id = atoi(tree_split_node[0].c_str());
			for(int i = 1; i < config.query_num+1; i++)
			{
				int node_id = atoi(tree_split_node[i].c_str());
				node_id_spilt_to[tree_id][i-1] = node_id;
			}
		}
	}

	void fit_data(string str)
	{
		vector<string> str_valueshares = SplitString(str, '\n');
		for(auto str_valueshare : str_valueshares)
		{
			vector<string> str_values = SplitString(str_valueshare, ',');
			for(int i = 0; i < str_values.size(); i++)
			{
				row_data[test_features[i]].push_back(atoi(str_values[i].c_str()));
			}
		}
	}

	pair<vector<int>, vector<int>> get_shold_value(int tree_id)
	{
		vector<int> sholds,values;
		for(int i = 0; i < config.query_num; i++)
		{
			int cur_node_id = node_id_spilt_to[tree_id][i];
			int shold = boost[tree_id][cur_node_id].threshold; //第i个样本所到达划分节点的阈值
			sholds.push_back(shold);
			int value = row_data[boost[tree_id][cur_node_id].feature][i]; //第i个样本所到达划分节点的特征所对应的特征值共享份额
			values.push_back(value);
		}
		return make_pair(sholds, values);
	}

	string Jsondump_svboost()
	{
		string str_xgboost;
		for(auto xtree : boost)
		{
			string str_tree(to_string(xtree.first)+"_th tree:\n");
			for(auto node : xtree.second)
			{
				str_tree += to_string(node.first)+": {";
				str_tree += "feature:" + to_string(node.second.feature) + ",";
				str_tree += "threshold:" + to_string(node.second.threshold) + "}\n";
			}
			str_xgboost += str_tree;
		}
		cout << str_xgboost << endl;
		cout << endl;
		return str_xgboost;

	}

	void save_svboost(string filepath)
	{
		filesystem::create_directories(filesystem::path(filepath).parent_path());
		string str_xgboost =  Jsondump_svboost();
		ofstream myfile(filepath);   

		if (myfile.is_open()) {  
			myfile << str_xgboost;  
			myfile.close();      
		} else {
			cout << "Unable to open file";  
		}
	}

	void Jsonload_svboost(string filepath)
	{
		vector<string> lines;
		ifstream file(filepath);
		int tree_id, node_id;
		if (file.is_open()) {
			string line;
			while (getline(file, line)) {
				stringstream ss(line);
				vector<double> nums;

				char c;
				while (ss >> c) {
					if (isdigit(c) || c == '.' || c == '-') {
						string num_str;
						num_str += c;
						while (ss >> c && (isdigit(c) || c == '.')) {
							num_str += c;
						}
						double num = stod(num_str);
						nums.push_back(num);
					}
				}
				if(nums.size() == 1)
				{
					tree_id = int(nums[0]);
				}
				else
				{
					node_id = int(nums[0]);
					boost[tree_id][node_id].feature = (Features)(int)nums[1];
					boost[tree_id][node_id].threshold = (int)nums[2];
				}

			}
			file.close();
		}
	}

};

#endif