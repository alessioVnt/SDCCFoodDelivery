
#Generate pb files from proto.proto in ../pb folder

ATH=$PATH:$GOPATH/bin
protodir=../pb

protoc --go_out=plugins=grpc:pb -I $protodir $protodir/proto.proto
