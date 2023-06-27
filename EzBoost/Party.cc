#include "utils_common.hpp"
#include "utils_party.hpp"
#include "xg_boost.hpp"

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
		vector<string> elements = SplitString(line, ',');
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
		fileout_party.close();
	}


}

void Acp_Calculate_Grad_Hess(int r, MyParty& demo, XGBoost& booster)
{
	cout << "主动参与方正在计算本次迭代的梯度，并秘密共享给S1和S2......" << endl;
	booster.CalculateGradHess(r, demo.data[LABEL]);
	GradAndHess_list GH_list_a, GH_list_b;
	booster.Share_Gradients_list(booster.gradhess_list, GH_list_a, GH_list_b);
	string str_GL_a = to_string(GLtype)+sep+to_string(acp)+bline;
	str_GL_a += JsonDump_Gradients_list(GH_list_a);
	string str_GL_b = to_string(GLtype)+sep+to_string(acp)+bline;
	str_GL_b += JsonDump_Gradients_list(GH_list_b);
	timer.wait();
	comer.add(str_GL_a.length()+str_GL_b.length());
	zmq_send_msg(str_GL_a, S1);
	zmq_send_msg(str_GL_b, S2);
	cout << "主动参与方已经完成计算梯度，并已经秘密共享给S1和S2！" << endl;
}

void Acp_Recieve_MuxGH(Entity p,int t, MyParty &demo)
{
	int tag = 0;
	zmq::context_t ctx(1);
    zmq::socket_t socket (ctx, ZMQ_DEALER);
    socket.bind(ips[p]); 
	cout << "主动参与方正在接受S1和S2的梯度与桶乘积的共享........." << endl;
	timer.record();
	while(1)
	{
		zmq::message_t request;
        bool ok = socket.recv(&request);
        if(ok){
            char* buffer = new char[request.size()+1];
            memcpy(buffer , request.data (), request.size());
			buffer[request.size()] = '\0';
           	string recv(buffer);
			auto type = (TranTypes)get_int_until_chr(recv, '\n');
			if (type == GLtype)
			{
				demo.Store_GradHess_sum(tag, t, recv);
				zmq::message_t reply (5);
				memcpy (reply.data (), "close", 5);
				socket.send (reply);
				if(tag == 2)
				{
					break;
				}
			} 
        }
	}
	timer.show();
	cout << "主动参与方已经恢复了梯度与桶的乘积！" << endl;
}

vector<int> Acp_predicting(Entity p, MyParty& demo, XGBoost& booster)
{
	vector<double> leaf_values;
	for(int i = 0; i < config.query_num; i++)
	{
		leaf_values.push_back(0.0);
	}

	zmq::context_t ctx(1);
    zmq::socket_t socket (ctx, ZMQ_DEALER);
    socket.bind(ips[acp]); 
	unordered_map<int, vector<int>> tree_split_node; //第一个int是树id，vector<int>是所有查询数据要分到的节点id
	unordered_map<int, vector<bool>> tree_split_cmp; //第一个int是树id，vector<bool>是所有查询数据在当前划分下的比较结果
	for(int i = 1; i <= config.max_tree_num; i++)
	{
		for(int j = 0; j < config.query_num; j++)
		{
			tree_split_node[i].push_back(1);
			tree_split_cmp[i].push_back(false);
		}
	}
	int tag = 0;
	int skip = 0;
	while(1)
	{
		zmq::message_t request;
        bool ok = socket.recv(&request);
        if(ok){
            char* buffer = new char[request.size()+1];
            memcpy(buffer , request.data (), request.size());
			buffer[request.size()] = '\0';
           	string recv(buffer);
			auto type = (TranTypes)get_int_until_chr(recv, '\n');
			if(type == Comparison)
			{
				zmq::message_t reply (5);
				memcpy (reply.data (), "close", 5);
				socket.send (reply);
				vector<string> str_tree_cmp_ress = SplitString(recv, '\n');

				for(auto str_tree_cmp_res : str_tree_cmp_ress)
				{
					vector<string> tree_cmp_res = SplitString(str_tree_cmp_res, '-');
					int tree_id = atoi(tree_cmp_res[0].c_str());
					for(int i = 1; i < tree_cmp_res.size(); i++)
					{
						bool cmp_res = (bool)atoi(tree_cmp_res[i].c_str());
						tree_split_cmp[tree_id][i-1] = tree_split_cmp[tree_id][i-1] ^ cmp_res;
					}
				}
				tag ++;
				if(tag == 2)
				{
					string str_tree_split_node(to_string(NodeID)+"\n");
					for(int i = 1; i <= config.max_tree_num; i++)
					{
						str_tree_split_node += to_string(i);
						assert(tree_split_cmp[i].size() == config.query_num);
						for(int j = 0; j < config.query_num; j++)
						{
							if(!tree_split_cmp[i][j])
								tree_split_node[i][j] = 2*tree_split_node[i][j];
							else
								tree_split_node[i][j] = 2*tree_split_node[i][j]+1;
							if(booster.boost[i][tree_split_node[i][j]].is_leaf)
							{
								leaf_values[j] += config.learning_rate*booster.boost[i][tree_split_node[i][j]].leaf_value;
								skip++;
							}
							else
							{
								str_tree_split_node += "," + to_string(tree_split_node[i][j]);
							}
						}
						str_tree_split_node += bline;
					}
					if(skip == config.max_tree_num*config.query_num) break;
					comer.add(str_tree_split_node.length()*2);
					zmq_send_msg(str_tree_split_node, S1);
					zmq_send_msg(str_tree_split_node, S2);
					for(int i = 1; i <= config.max_tree_num; i++)
					{
						for(int j = 0; j < config.query_num; j++)
						{
							tree_split_cmp[i][j] = false;
						}
					}
					tag = 0;
				}
			}
			
        }
		
	}
	vector<int> predictions;
	for(int j = 0; j < config.query_num; j++)
	{
		// cout << j << "被分到的节点为" <<tree_split_node[2][j] << endl;

		double p_0 = 1.0 / (1 + exp(2 * leaf_values[j]));
		int pre = 1 - p_0 >= 0.5 ? 1 : 0;
		// cout << "预测为" << pre << endl;
		predictions.push_back(pre);
	}
	return predictions;
}

int main(int argc, char **argv)
{
	random_data();
	int r = 0;
	string filepath = model_path+"/acp_xgboost_"+to_string(config.max_tree_num)+"_"+to_string(config.max_depth)+"_"+to_string(config.learning_rate)+".boost";
	unordered_map<Entity, MyParty> parties;
	for(auto p : client)
	{
		parties[p] = MyParty(p);
	}
	XGBoost booster;
	string sep("-");
	auto train_start = timer.now();
	if(filesystem::exists(filepath))
	{
		booster.Jsonload_xgboost(filepath);
	}
	else
	{
		
		while(r < config.max_tree_num)
		{
			r++;
			cout << "============================== 正在建立第"<<to_string(r) <<"棵树 ==============================" << endl;
			// auto clock_start = timer.now();
			if(r != 1)
			{
				booster.clear();
				for(auto p : client)
				{
					parties[p].clear();
				}
			} 

			Acp_Calculate_Grad_Hess(r, parties[acp], booster);

			int t = 0;
			while(t < booster.max_node_id)
			{
				t++;
				if(booster.tree[t / 2].is_leaf)
				{
					booster.construct_tree(r, t);
					continue;
				}
				
				cout << "=============== 正在建立第"<<to_string(r) <<"棵树的第"<< to_string(t) << "个节点 ===============" << endl;
				if(t >= booster.min_leaf_node_id)
				{
					bool hasTrue = any_of(parties[acp].node_samples_onehot[t].begin(), parties[acp].node_samples_onehot[t].end(), [](bool b) { return b; });
					if(hasTrue)
						booster.construct_leafnode(r, t, parties[acp].node_samples_onehot[t]);
					booster.construct_tree(r, t);
					continue;
				}
				
				string str_samples_onehot(to_string(SOtype)+sep+to_string(acp)+bline);
				str_samples_onehot += JsonDump_samples_onehot(parties[acp].node_samples_onehot[t]);
				timer.wait();
				comer.add(str_samples_onehot.length());
				zmq_send_msg(str_samples_onehot, S1);
				zmq_send_msg(str_samples_onehot, S2);
				Acp_Recieve_MuxGH(acp, t, parties[acp]);
				parties[acp].Mul_BM_GH(t, booster.gradhess_list);

				bool is_leaf = false;
				string best_split = booster.Find_best_split(t, parties[acp].GradAndHess_Lsum[t], is_leaf);
				// cout << best_split << " : ";
    		
				if(!is_leaf)
				{
					Entity p_star = (Entity)get_int_until_chr(best_split, '-');
					Features d_star = (Features)get_int_until_chr(best_split, '-');
					int i_star = atoi(best_split.c_str());
					// cout << parties[p_star].threshold_sets[d_star][i_star] << endl;

					auto threshold_share = parties[p_star].Share_thres holds(t, d_star, i_star);
					string str_TS_a = to_string(TStype)+sep+to_string(p_star)+bline+to_string(t)+sep+to_string(d_star)+sep+to_string(threshold_share.first);
					string str_TS_b = to_string(TStype)+sep+to_string(p_star)+bline+to_string(t)+sep+to_string(d_star)+sep+to_string(threshold_share.second);
					comer.add(str_TS_a.length()+str_TS_b.length());
					zmq_send_msg(str_TS_a, S1);
					zmq_send_msg(str_TS_b, S2);
					
					for(auto p : client)
					{
						if(p == p_star) continue;
						parties[p].update_samples_onehot(t, parties[p_star].node_samples_onehot);
						comer2.add(JsonDump_samples_onehot(parties[p_star].node_samples_onehot[2*t]).length() + JsonDump_samples_onehot(parties[p_star].node_samples_onehot[2*t+1]).length());
					}
					bool hasTrue = any_of(parties[p_star].node_samples_onehot[t].begin(), parties[p_star].node_samples_onehot[t].end(), [](bool b) { return b; });
					if(hasTrue)
						booster.construct_splitnode(r, t, p_star, d_star, i_star);
					booster.construct_tree(r, t);
				}
				else
				{
					bool hasTrue = any_of(parties[acp].node_samples_onehot[t].begin(), parties[acp].node_samples_onehot[t].end(), [](bool b) { return b; });
					if(hasTrue)
						booster.construct_leafnode(r, t, parties[acp].node_samples_onehot[t]);
					booster.construct_tree(r, t);
				}
			}
			
			booster.Jsondump_xgboost();
			//parties[acp].show_situation();
			
			
			string newtree = to_string(NewTree)+sep+to_string(acp)+bline+to_string(r);
			zmq_send_msg(newtree, S1);
			zmq_send_msg(newtree, S2);
			// auto clock_end = timer.now();
			// cout << "第" << r << "棵树构建";
			// timer.compute(clock_start, clock_end);
		}
		zmq_send_msg(to_string(ShutDown)+sep+to_string(acp)+bline, S1);
		zmq_send_msg(to_string(ShutDown)+sep+to_string(acp)+bline, S2);
		
	}
	auto train_end = timer.now();
	cout << "训练过程";
	timer.compute(train_start, train_end, timer.waitting_time);
	comer.show("训练过程主动方");
	comer2.show("训练过程被动方");
	cout << "训练完成，请运行User端进行查询！" << endl;
	// booster.save_xgboost(filepath);

	zmq_send_msg(to_string(Start)+bline, U);
	
	vector<int> preds = Acp_predicting(acp, parties[acp], booster);

	string query_result(to_string(Prediction)+"\n");
	for(int j = 0; j < preds.size();j++)
	{
		query_result += to_string(preds[j]);
	}
	comer.add(query_result.length());
	zmq_send_msg(query_result, U);
	cout << "sent prediction" << endl;

	zmq_send_msg(to_string(ShutDown)+sep, S1);
	zmq_send_msg(to_string(ShutDown)+sep, S2);
	comer.show("主动方查询");
	return 0;
}
