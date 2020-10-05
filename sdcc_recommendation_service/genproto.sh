
#Generate pb files from proto.proto in ../pb folder

python3 -m grpc_tools.protoc -I ../pb --python_out=. --grpc_python_out=. ../pb/proto.proto

