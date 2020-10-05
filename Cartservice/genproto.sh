set -e

PROTODIR=../pb

# enter this directory
CWD="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

protoc --plugin=protoc-gen-grpc=/home/alessio/grpc/bins/opt/grpc_csharp_plugin --csharp_out=$CWD/grpc_generated --grpc_out=$CWD/grpc_generated -I $PROTODIR $PROTODIR/proto.proto
