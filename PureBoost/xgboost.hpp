#ifndef XGBOOST_H
#define XGBOOST_H

#include "utils-common.hpp"
#define magnify 1000
#define roundsize 3

class XGBoost {
public:
	struct APNode {
		bool is_leaf;
		Entity split_Entity;
		Features split_feature;
		int split_threshold;
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
	map<int, map<string, GradAndHess>> split_GH_sum;
	unordered_map<int, map<int,int>> id_in_leafnode;//第一个int是树id,第二个int是样本id，第三个int是叶节点id
public: 
	XGBoost(){
		min_leaf_node_id = pow(2, config.max_depth-1);
		max_node_id = pow(2, config.max_depth) - 1;
		gsum[1] = 0, hsum[1] = 0;
	}
	~XGBoost(){}

	GradAndHess_list CalculateGradHess(int tree_id, vector<int> labels)
	{
		GradAndHess_list GH_list;
		GradAndHess GH;
		if(tree_id == 1)
		{
			cout << "=============================== Constructing the 1-th tree of XGBoost ===============================" << endl;
			double sum = 0;
			for(auto label : labels)
			{
				sum += label;
			}
			double mean = sum / (double)config.samples_num;
			y_pred = 0.5 * log((1 + mean) / (1 - mean));
			for(int i = 0; i < config.samples_num; i++)
			{
				int y = labels[i];
				
				double pred = 1.0 / (1.0 + exp(-y_pred));
				double grad = (-y + (1 - y) * exp(pred)) / (1 + exp(pred));
				double round_grad = round_double(grad, roundsize);
				double hess = exp(pred) / pow((1 + exp(pred)), 2);
				double round_hess = round_double(hess, roundsize);
				gsum[1] += round_grad, hsum[1] += round_hess;
				GH = {round_grad, round_hess};
				// cout << (int) gradient.grad << endl;
				GH_list.push_back(GH);
			}
			
		}
		else
		{
			cout << "=============================== Constructing the " << to_string(tree_id) << "-th tree of XGBoost ===============================" << endl;
			for(int i = 0; i < config.samples_num; i++)
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
				GH = {round_grad, round_hess};
				// cout << (int) gradient.grad << endl;
				GH_list.push_back(GH);
			}
		}
		cout << "CalculateGradHess already!" << endl;
		return GH_list;
	};

	void merge_Lsum(int node_id, unordered_map<string, GradAndHess> GH_split_sum)
	{
		split_GH_sum[node_id].insert(GH_split_sum.begin(), GH_split_sum.end());
	}

	string Find_best_split(int node_id, bool & is_leaf)
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
					string ident = to_string(p)+"-"+to_string(d)+"-"+to_string(i);
					auto sum = split_GH_sum[node_id][ident];
					gL += sum.grad;
					hL += sum.hess;
					double gR = gsum[node_id]-gL;
					double hR = hsum[node_id]-hL;
					double gain = pow(gL, 2) / (hL + lambda) + pow(gR, 2) / (hR + lambda) - pow(gsum[node_id],2) / (hsum[node_id]+lambda);
					if(ident == "3-1-0")
					{
						cout << " ";
					}
					if(gain-score> 0.0001)
					{
						score = gain;
						best_key = ident;
						if(gR == 0)
						{
							is_leaf = true;
						}
						else
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
		cout << "最佳划分增益值为" << score << endl;
		cout << (is_leaf?"是":"不是") << "叶节点" << endl;
		return best_key;
	}

	void construct_splitnode(int r, int node_id, Entity en, Features fea, int threshold)
	{
		APNode node{false, en, fea, threshold, 0};
		
		tree[node_id] = node;
		cout << "=============== 第 " << to_string(r) << "棵树的第" << to_string(node_id) << "个节点构建完成 ===============" << endl;
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
			cout << "=============================== The " << to_string(r) << "-th tree of XGBoost Constructed ===============================" << endl;
		}
	}

	unordered_map<int, int> predict(unordered_map<Features, vector<int>> test_data)
	{
		unordered_map<int, int> prediction;
		for(int i = 0; i < 6000; i++)
		{
			double pred = 0;
			for(int r = 1; r <= boost.size(); r++)
			{
				int t = 1;
				while(boost[r][t].is_leaf == false)
				{
					int value = test_data[boost[r][t].split_feature][i];
					if(value <= boost[r][t].split_threshold)
					{
						t = 2*t;
					}
					else
					{
						t = 2*t+1;
					}
				}
				
				cout << i << "在" << r << "被分到的节点为" << t << endl;
				pred += config.learning_rate * boost[r][t].leaf_value;
			}

			double p_0 = 1.0 / (1 + exp(2 * pred));
			prediction[i] = 1 - p_0 >= 0.5 ? 1 : 0;
			cout << "预测为" << prediction[i] << endl;
		}
		return prediction;
	}
	void Jsondump_xgboost()
	{
		string str_xgboost;
		for(auto xtree : boost)
		{
			string str_tree(to_string(xtree.first)+"-th tree:\n");
			for(auto node : xtree.second)
			{
				str_tree += to_string(node.first)+": {";
				str_tree += "is_leaf:" + to_string(node.second.is_leaf) + ",";
				str_tree += "split_Entity:" + to_string(node.second.split_Entity) + ",";
				str_tree += "split_feature:" + to_string(node.second.split_feature) + ",";
				str_tree += "split_threshold_id:" + to_string(node.second.split_threshold) + ",";
				str_tree += "leaf_value:" + to_string(node.second.leaf_value) + "}\n";
			}
			str_xgboost += str_tree;
		}
		cout << str_xgboost << endl;
		cout << endl;
	}

	void clear()
	{
		split_GH_sum.clear();
		gsum.clear();
		hsum.clear();
	}
};

class SVBoost {
private:
	typedef unordered_map<int, int> svtree;
	int node_id = 0;
	int tree_id = 0;
	svtree tree;
	unordered_map<int, svtree> boost;
	int max_node_id;

public:
	SVBoost(){
		max_node_id = pow(2,config.max_depth)-1;
	}
	~SVBoost(){}
	void construct_node(int threshold_share)
	{
		node_id++;
		if(node_id == max_node_id)
		{
			tree_id++;
			boost[tree_id] = tree;
			tree.clear();
			node_id = 1;
		}
		tree[node_id] = threshold_share;
	}
};

#endif