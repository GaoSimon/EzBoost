#ifndef SERVER_H
#define SERVER_H

#include <iostream>
#include <BuildingBlocks/aux-protocols.h>
#include <utils/emp-tool.h>
#include <LinearOT/linear-ot.h>
#include <bitset>
#include "utils_common.hpp"
#include "xg_boost.hpp"


using namespace std;
using namespace sci;

int party, port = 32000;
string address = "127.0.0.1";
uint64_t train_com = 0;

class Server {
public:
	Entity sv;
	IOPack *my_iopack;
	OTPack *my_otpack;
	LinearOT *prod;
	PRG128 *prg;
	AuxProtocols *my_aux;
	vector<uint64_t> grad_list, hess_list;
	unordered_map<string, Mymat> Received_BM_share;
	unordered_map<string, GradAndHess> GradAndHess_sum_a; 
	unordered_map<string, vector<int>> Received_TS_share;
public:
	Server(int argc, char **argv, int r)
	{
		if(r == 1) sv = S1; else sv=S2;
		argc = 2;
		const char *file_char = r == 1 ? "r=1" : "r=2";
		argv[1] = const_cast<char*>(file_char);
		ArgMapping amap;
		amap.arg("r", party, "Role of party: ALICE = 1; BOB = 2");
		amap.arg("p", port, "Port Number");
		amap.arg("ip", address, "IP Address of server (ALICE)");
		amap.parse(argc, argv);

		my_iopack = new IOPack(party, port, address);
		my_otpack = new OTPack(my_iopack, party);

		prod = new LinearOT(party, my_iopack, my_otpack);
		prg = new PRG128();

		my_aux = new AuxProtocols(party, my_iopack, my_otpack); 
	}
	~Server(){}

	void Store_GL(GradAndHess_list GL)
	{
		for(auto gradient : GL)
		{
			grad_list.push_back(gradient.grad);
			hess_list.push_back(gradient.hess);
		}
	}

	void Store_BM(Entity p, Features d, Mymat BM_share)
	{
		Received_BM_share[to_string(p)+sep+to_string(d)] = BM_share;
	}

	void Store_TS(Entity p, Features d, vector<int> TS_share)
	{
		Received_TS_share[to_string(p)+sep+to_string(d)] = TS_share;
	}

	//用GL的份额和BM的份额相乘，不同客户有不同维度，是两个grad和hess值
	string Mux_BM_GL(Myvec onehot)
	{
		string str;
		for(auto p : client)
		{
			if(p == P1)
				continue;
			for(auto d : individual_features[p])
			{
			
				if(d == ID || d == LABEL)
					continue;
				string ident = to_string(p)+sep+to_string(d);
				Mymat BM_share = Received_BM_share[ident];
				for(int i = 0; i < BM_share.rows(); i++)
				{
					BM_share.row(i) = BM_share.row(i) && onehot;
				}
				
				vector<pair<uint64_t,uint64_t>> gradhess_sum_a_set = F_MUX(BM_share, grad_list, hess_list, BM_share.rows() * BM_share.cols()); 

				for(int i = 0; i < gradhess_sum_a_set.size(); i++)
				{
					str+= ident+sep+to_string(i) +"$" + to_string(gradhess_sum_a_set[i].first)+ sep + to_string(gradhess_sum_a_set[i].second) + bline;
				}
			}
		}
		
		return str;
	}
	
	vector<pair<uint64_t,uint64_t>> F_MUX(Mymat& a, vector<uint64_t>& c,vector<uint64_t>& d, int dim)
	{
		auto sel = new uint8_t[2*dim];
		auto x = new uint64_t[2*dim];
		auto z = new uint64_t[2*dim];
		int row = a.rows();
		int col = a.cols();
		
		int k = 0;
		for(int i = 0; i < row; i++)
		{
			for(int j = 0; j < col; j++)
			{
				sel[k]= a(i,j) == true ? 1 : 0;
				sel[dim+k]= a(i,j) == true ? 1 : 0;
				x[k] = c[j] & mask;
				x[dim+k] = d[j] & mask;
				k++;
			}
		}
		// comer.add(2*dim*sizeof(uint8_t)+2*2*dim*sizeof(uint64_t));
		my_aux->multiplexer(sel, x, z, 2*dim, bw, bw);
		
		vector<pair<uint64_t,uint64_t>> res_set;
		for(int i = 0; i < row; i++)
		{
			uint64_t grad_res = 0, hess_res = 0;
			for(int j = 0; j < col; j++)
			{
				grad_res = (grad_res + z[i*col+j] & mask) & mask;
				hess_res = (hess_res + z[dim+i*col+j] & mask) & mask;
			}
            res_set.push_back(make_pair(grad_res, hess_res));
		}
		delete sel;
		delete x;
		delete z;
		return res_set;
	}

	vector<bool> F_MILL(vector<int> threshold, vector<int> values)
	{
		assert(values.size() == config.query_num);
		int dim = config.query_num;
		auto a = new uint64_t[dim];
		auto r_alpha= new uint64_t[dim];
		auto res= new uint8_t[dim];
		if(sv == S1)
		{
			for(int i = 0; i < dim; i++)
			{
				a[i] = (uint64_t)rand();
				r_alpha[i] = (uint64_t)values[i] - (uint64_t)threshold[i]  + a[i];
			}
			my_iopack->io->send_data(r_alpha, dim* sizeof(uint64_t));
			my_aux->mill->compare(res, a, dim, bw, false);
			// my_iopack->io->send_data(res, dim* sizeof(uint8_t));
		}
		else
		{
			my_iopack->io->recv_data(r_alpha, dim* sizeof(uint64_t));
			auto r_beta = new uint64_t[dim];
			auto r = new uint64_t[dim];
			for(int i = 0; i < dim; i++)
			{
				r_beta[i] = (uint64_t)values[i] - (uint64_t)threshold[i];
				r[i] = r_alpha[i] + r_beta[i];			
			}
			my_aux->mill->compare(res, r, dim, bw, false);
			// auto res0 = new uint8_t[dim];
			// my_iopack->io->recv_data(res0, dim* sizeof(uint64_t));
			// for(int i = 0; i < dim; i++)
			// {
			// 	cout << (char)('0'+res[i]^res0[i]) << endl;
			// }
			// cin.get();
		}

		vector<bool> ret;
		for(int i = 0; i < dim; i++)
		{
			ret.push_back((bool)res[i]);
		}
		return ret;

	}
	
	void clear()
	{
		grad_list.clear();
		hess_list.clear();
	}
};

void Server_Boost_training(string ip, Server& demo, SVBoost& booster)
{
	while(1)
	{
		zmq::context_t ctx(1);
		zmq::socket_t socket (ctx, ZMQ_DEALER);
		socket.bind (ip); 
		zmq::message_t request;
		bool ok = socket.recv(&request);
		if(ok){
			char* buffer = new char[request.size()+1];
			memcpy(buffer , request.data(), request.size());
			buffer[request.size()] = '\0';
			string recv(buffer);
			TranTypes type = (TranTypes)get_int_until_chr(recv, '-');
			Entity p = (Entity)get_int_until_chr(recv, '\n');
			if(type == GLtype)
			{
				zmq::message_t reply (5);
				memcpy (reply.data (), "close", 5);
				socket.send (reply);
				GradAndHess_list GL_a = JsonLoad_Gradients_list(recv);
				demo.Store_GL(GL_a);
			}
			else if(type == BMtype)
			{
				zmq::message_t reply (5);
				memcpy (reply.data (), "close", 5);
				socket.send (reply);
				vector<string> str_BM_a_s = SplitString(recv, '$');
				for(auto str_BM_a : str_BM_a_s)
				{
					Features d = (Features)get_int_until_chr(str_BM_a, '-');
					Mymat BM_a = JsonLoad_Bucket_Matrix(str_BM_a);
					demo.Store_BM(p,d,BM_a);
				}			
			}
			else if(type == SOtype)
			{
				zmq::message_t reply (5);
				memcpy (reply.data (), "close", 5);
				socket.send (reply);
				Myvec samples_onehot = JsonLoad_samples_onehot(recv);
				string str_gradients_sum_a(to_string(GLtype)+"\n");
				str_gradients_sum_a += demo.Mux_BM_GL(samples_onehot);
				comer.add(str_gradients_sum_a.length());
				zmq_send_msg( str_gradients_sum_a, acp);
				cout << "服务器已完成传输乘积！" << endl;
			}
			else if(type == TStype)
			{
				zmq::message_t reply (5);
				memcpy (reply.data (), "close", 5);
				socket.send (reply);
				int node_id = get_int_until_chr(recv, '-');
				Features fea = (Features)get_int_until_chr(recv, '-');
				int threshold_a = atoi(recv.c_str());
				booster.construct_node(node_id, fea, threshold_a);
				cout << "服务器构建了第" << node_id << "个划分节点！" << endl;
			}
			else if(type == NewTree)
			{
				zmq::message_t reply (5);
				memcpy (reply.data (), "close", 5);
				socket.send (reply);
				int tree_id = atoi(recv.c_str());
				booster.construct_tree(tree_id);
				demo.clear();
			}
			else if(type == ShutDown)
			{
				zmq::message_t reply (5);
				memcpy (reply.data (), "close", 5);
				socket.send (reply);
				cout << "EzPC通信" << demo.my_iopack->get_comm() << endl;
				train_com = demo.my_iopack->get_comm();
				comer.add(demo.my_iopack->get_comm());
				comer.show("服务器训练过程");
				return;
			}			
		}
		else
		{
			cout << "S1 recv message error!" << endl;
			return;
		}
		
	}

	
}

void Server_predicting(string ip, Server& demo, SVBoost& booster)
{
	while(1)
	{
		zmq::context_t ctx(1);
		zmq::socket_t socket (ctx, ZMQ_DEALER);
		socket.bind (ip); 
		zmq::message_t request;
		bool ok = socket.recv(&request);
		if(ok){
			cout << "Recved message" << endl;
			char* buffer = new char[request.size()+1];
			memcpy(buffer , request.data(), request.size());
			buffer[request.size()] = '\0';
			string recv(buffer);
			TranTypes type = (TranTypes)get_int_until_chr(recv, '\n');
			if(type == Query)
			{
				
				zmq::message_t reply (5);
				memcpy (reply.data (), "close", 5);
				socket.send (reply);
				booster.fit_data(recv);
				string str_tree_cmp_res(to_string(Comparison)+"\n");
				for(int tree_id = 1; tree_id <= config.max_tree_num; tree_id++)
				{
					str_tree_cmp_res += to_string(tree_id);
					auto shold_value = booster.get_shold_value(tree_id);
					vector<bool> cmp_ress = demo.F_MILL(shold_value.first, shold_value.second);
					for(int i = 0; i < config.query_num; i++)
					{
						str_tree_cmp_res += sep+to_string(cmp_ress[i]);
					}
					str_tree_cmp_res += bline;
				}
				comer.add(str_tree_cmp_res.length());
				zmq_send_msg( str_tree_cmp_res, acp);
			}
			else if(type == NodeID)
			{
				zmq::message_t reply (5);
				memcpy (reply.data (), "close", 5);
				socket.send (reply);
				booster.update_query_node_id(recv);
				string str_tree_cmp_res(to_string(Comparison)+bline);
				for(int tree_id = 1; tree_id <= config.max_tree_num; tree_id++)
				{
					str_tree_cmp_res += to_string(tree_id);
					auto shold_value = booster.get_shold_value(tree_id);
					vector<bool> cmp_ress = demo.F_MILL(shold_value.first, shold_value.second);
					for(int i = 0; i < config.query_num; i++)
					{
						str_tree_cmp_res += sep+to_string(cmp_ress[i]);
					}
					str_tree_cmp_res += bline;
				}
				comer.add(str_tree_cmp_res.length());
				zmq_send_msg(str_tree_cmp_res, acp);
			}
			else if(type == ShutDown)
			{

				zmq::message_t reply (5);
				memcpy (reply.data (), "close", 5);
				socket.send (reply);
				
				cout << "EzPC通信" << demo.my_iopack->get_comm()-train_com << endl;
				comer.add(demo.my_iopack->get_comm()-train_com);
				comer.show("服务器查询过程");
				return;
			}
			
		}
		
		
	}
}



#endif
