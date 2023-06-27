#ifndef COMMON_H
#define COMMON_H

#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <pthread.h>
#include <cmath>
#include <numeric>
#include <algorithm>
#include <random>
#include <functional>
#include <mutex>
#include <set>
#include <chrono>
#include <omp.h>
#include <unordered_set>
#include <map>
#include <unordered_map>
#include <filesystem> 
#include <eigen3/Eigen/Dense>
#include "zmq.hpp"
#include "zmq_addon.hpp"

using namespace Eigen;
using namespace std;
#define N 4
class Config {
public:
	const int init_threshold_num = 32;
	const int samples_num = 24000;
	const int max_tree_num = 2;
	const int max_depth = 6;
	const double learning_rate = 0.3;
};
const Config config;
class Timer{
public:
	chrono::time_point<chrono::high_resolution_clock, chrono::nanoseconds> last_time;
	int waitting_time = 0;
	void record()
	{
		last_time = chrono::high_resolution_clock::now();
	}
	void show()
	{
		auto duration = chrono::high_resolution_clock::now()-last_time;
		cout << "时间开销: " << duration.count()/ 1000000.0 << " ms." << endl;
	}
	chrono::time_point<chrono::high_resolution_clock, chrono::nanoseconds> now()
	{
		return chrono::high_resolution_clock::now();
	}
	void compute(chrono::time_point<chrono::high_resolution_clock, chrono::nanoseconds> start, chrono::time_point<chrono::high_resolution_clock, chrono::nanoseconds> end, int waitting_time = 0)
	{	
		auto duration = end-start;
		cout << "时间开销: " << (duration.count()/ 1000000.0 - waitting_time*1000) << " ms." << endl;
	}
	void wait()
	{
		sleep(1);
		waitting_time++;
	}
};
Timer timer;

void show_done(string str)
{
	cout << "The function of " << str << " has done!" << endl;
}

enum Entity {S1,S2,P1,P2,P3,P4,U,Noen=-1};
Entity client[N] = {P1,P2,P3,P4};
Entity acp = P1;
enum Features {ID,LIMIT_BAL,SEX,EDUCATION,MARRIAGE,AGE,
	PAY_1,PAY_2,PAY_3,PAY_4,PAY_5,PAY_6,
	BILL_AMT1,BILL_AMT2,BILL_AMT3,BILL_AMT4,BILL_AMT5,BILL_AMT6,
	PAY_AMT1,PAY_AMT2,PAY_AMT3,PAY_AMT4,PAY_AMT5,PAY_AMT6,LABEL,Nofea=-1};

enum TranTypes {BMtype, GLtype, BStype, TStype};

vector<string> ips = {"tcp://127.0.0.1:4001", "tcp://127.0.0.1:4002", "tcp://127.0.0.1:5001", 
					  "tcp://127.0.0.1:5002", "tcp://127.0.0.1:5003", "tcp://127.0.0.1:5004"};
typedef Array<bool, 1, Dynamic> Myvec;
typedef Array<bool, Dynamic, Dynamic> Mymat;
struct GradAndHess {
	double grad;
	double hess;
};
typedef vector<GradAndHess> GradAndHess_list; 
unordered_map<Entity, vector<Features>> individual_features = {
	{P1, {ID,PAY_AMT1,PAY_AMT2,PAY_AMT3,PAY_AMT4,PAY_AMT5,PAY_AMT6,LABEL}},
	{P2, {ID,LIMIT_BAL,SEX,EDUCATION,MARRIAGE,AGE}},
	{P3, {ID,PAY_1,PAY_2,PAY_3,PAY_4,PAY_5,PAY_6}},
	{P4, {ID,BILL_AMT1,BILL_AMT2,BILL_AMT3,BILL_AMT4,BILL_AMT5,BILL_AMT6}}
};
unordered_map<Entity, string> individual_datafilepath = {
	{P1, "../../data/Credit/use_dataset/APdata_train.csv"},
	{P2, "../../data/Credit/use_dataset/PPdata_train_0.csv"},
	{P3, "../../data/Credit/use_dataset/PPdata_train_1.csv"},
	{P4, "../../data/Credit/use_dataset/PPdata_train_2.csv"}
};
unordered_map<Entity, string> paths = {
		{P1, "../../data/Credit/random_dataset/datap1.csv"},
		{P2, "../../data/Credit/random_dataset/datap2.csv"},
		{P3, "../../data/Credit/random_dataset/datap3.csv"},
		{P4, "../../data/Credit/random_dataset/datap4.csv"}
};
unordered_map<Entity, string> attributes = {
	{P1, "ID,PAY_AMT1,PAY_AMT2,PAY_AMT3,PAY_AMT4,PAY_AMT5,PAY_AMT6,label"},
	{P2, "ID,LIMIT_BAL,SEX,EDUCATION,MARRIAGE,AGE"},
	{P3, "ID,PAY_1,PAY_2,PAY_3,PAY_4,PAY_5,PAY_6"},
	{P4, "ID,BILL_AMT1,BILL_AMT2,BILL_AMT3,BILL_AMT4,BILL_AMT5,BILL_AMT6"}
};
int bwA = 32;
int bwB = 32;
int bwC = 32;
int bw = 64;

uint64_t maskA = (bwA == 64 ? -1 : ((1ULL << bwA) - 1));
uint64_t maskB = (bwB == 64 ? -1 : ((1ULL << bwB) - 1));
uint64_t maskC = (bwC == 64 ? -1 : ((1ULL << bwC) - 1));
uint64_t mask = (bw == 64 ? -1 : ((1ULL << bw) - 1));

// 使用字符分割
void Stringsplit(string str, const char split, vector<string>& res)
{
	istringstream iss(str);	// 输入流
	string token;			// 接收缓冲区
	while (getline(iss, token, split))	// 以split为分隔符
	{
		res.push_back(token);
	}
}

double round_double(double val, int precision) {
    double factor = pow(10, precision);
    return round(val * factor) / factor;
}

// string JsonDump_Gradients_list(Gradients_list gradients_list)
// {
// 	string json("");
// 	string sep("\n");
// 	for(auto gradient : gradients_list){
// 		string str_gradient = to_string(gradient.first) + "-" + to_string(gradient.second.grad) + "-" + to_string(gradient.second.hess);
// 		json += str_gradient+sep;
// 	}
// 	// cout << "JsonDump_Gradients_list already!" << endl;
// 	return json;

// }

// Gradients_list JsonLoad_Gradients_list(string json)
// {
// 	Gradients_list gradients_list;
// 	vector<string> str_gradients_list;
// 	Stringsplit(json, '\n', str_gradients_list);
// 	for(string str_gradient : str_gradients_list)
// 	{
// 		vector<string> str_sub_gradient;
// 		Stringsplit(str_gradient, '-', str_sub_gradient);
// 		Gradients gradients = {strtoull(str_sub_gradient[1].c_str(), NULL, 10), strtoull(str_sub_gradient[2].c_str(), NULL, 10)};
// 		gradients_list[atoi(str_sub_gradient[0].c_str())] = gradients;
// 	}
// 	// cout << "JsonLoad_Gradients_list already!" << endl;
// 	return gradients_list;
// }



// string JsonDump_Bucket_Matrix(Bucket_Matrix bktmat)
// {
// 	string json("");
// 	string sep("\n");
// 	for(auto bkt : bktmat)
// 	{
// 		string str_bkt = "";
// 		for(auto b : bkt)
// 		{
// 			str_bkt += b == true ? string("1") : string("0");
// 		}
// 		json += str_bkt+sep;
// 	}
// 	// cout << "JsonDump_Bucket_Matrix already!" << endl;
// 	return json;
// }

// Bucket_Matrix JsonLoad_Bucket_Matrix(string json)
// {
// 	Bucket_Matrix bktmat;
// 	vector<string> str_bktmat;
// 	char sep = '\n';
// 	Stringsplit(json, sep, str_bktmat);
// 	for(auto str_bkt : str_bktmat)
// 	{
// 		Bucket bkt;
// 		for(auto c : str_bkt)
// 		{
// 			bkt.push_back(c == '1' ? true : false);
// 		}
// 		bktmat.push_back(bkt);
// 	}
// 	// cout << "JsonLoad_Bucket_Matrix already!" << endl;
// 	return bktmat;
// }


// void zmq_send_msg(string str, Entity target)
// {
// 	zmq::context_t context (1);
// 	zmq::socket_t socket (context, ZMQ_DEALER);
// 	string ip = ips[target];
// 	socket.connect (ip);
// 	zmq::message_t msg(str.size());
// 	memcpy(msg.data(), str.data(), str.size());
// 	if(!socket.connected())  //判断连接是否建立成功
// 	{
// 		std::cout << "Not connected." << std::endl;
// 	}
// 	bool ok = socket.send(msg);
	
// 	//  等待服务器返回的响应
// 	zmq::message_t reply;
// 	socket.recv(&reply);
// 	char* buffer = new char[reply.size()+1];
// 	memcpy(buffer, reply.data(), reply.size());
// 	buffer[reply.size()] = '\0';
// 	// cout << string(buffer) << endl;
// 	if(string(buffer) == "close")
// 	{
// 		socket.close();
// 		context.close();
// 		return;
// 	}
// 	else
// 	{
// 		cout << "error" << endl;
// 		return;
// 	}
// }
#endif