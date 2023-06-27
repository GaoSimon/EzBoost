#include "utils_user.hpp"
#include "utils_common.hpp"

void send_query_share(User& demo)
{
	string sep("-");
	
	string tree_values_a(to_string(Query)+"\n");
	string tree_values_b(to_string(Query)+"\n");

	auto value_shares = demo.get_valueshare_json(config.query_num);
	
	comer.add(tree_values_a.length()+value_shares.first.length()+tree_values_b.length()+value_shares.second.length());
	zmq_send_msg( tree_values_a+value_shares.first, S1);
	zmq_send_msg( tree_values_b+value_shares.second, S2);
	
}
int main()
{
	User user_demo;
	string ip = ips[U];
	int idx = 0;
	
	

	zmq::context_t ctx(1);
    zmq::socket_t socket (ctx, ZMQ_DEALER);
    socket.bind(ip); 
	while(1)
	{
		zmq::message_t request;
        bool ok = socket.recv(&request);
        if(ok){
            char* buffer = new char[request.size()+1];
            memcpy(buffer , request.data (), request.size());
			buffer[request.size()] = '\0';
           	string recv(buffer);
			TranTypes type = (TranTypes)get_int_until_chr(recv, '\n');
			if(type == Prediction)
			{
				zmq::message_t reply (5);
				memcpy (reply.data (), "close", 5);
				socket.send (reply);
				int cnt_right = 0;
				for(int i = 0; i < recv.length(); i++)
				{
					// cout << "Recv prediction: " << recv[i] << " ; ";
					// cout << "True label: " << user_demo.query_data[LABEL][i] << endl;
					if(recv[i] == '0'+user_demo.query_data[LABEL][i])
					{
						cnt_right++;
					}
				}
				cout << "查询总耗时";
				timer.show();
				cout << "测试准确率为: " << (double)cnt_right / (double)config.query_num << endl;
				comer.show("用户查询");
				return 0;
			}
			if(type == Start)
			{
				zmq::message_t reply (5);
				memcpy (reply.data (), "close", 5);
				socket.send (reply);
				timer.record();
				send_query_share(user_demo);
			}
        }
	}
}