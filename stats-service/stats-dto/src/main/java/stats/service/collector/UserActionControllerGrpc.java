package stats.service.collector;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.63.0)",
    comments = "Source: collector.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class UserActionControllerGrpc {

  private UserActionControllerGrpc() {}

  public static final java.lang.String SERVICE_NAME = "stats.service.collector.UserActionController";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<stats.service.collector.UserActionProto,
      com.google.protobuf.Empty> getCollectUserActionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CollectUserAction",
      requestType = stats.service.collector.UserActionProto.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<stats.service.collector.UserActionProto,
      com.google.protobuf.Empty> getCollectUserActionMethod() {
    io.grpc.MethodDescriptor<stats.service.collector.UserActionProto, com.google.protobuf.Empty> getCollectUserActionMethod;
    if ((getCollectUserActionMethod = UserActionControllerGrpc.getCollectUserActionMethod) == null) {
      synchronized (UserActionControllerGrpc.class) {
        if ((getCollectUserActionMethod = UserActionControllerGrpc.getCollectUserActionMethod) == null) {
          UserActionControllerGrpc.getCollectUserActionMethod = getCollectUserActionMethod =
              io.grpc.MethodDescriptor.<stats.service.collector.UserActionProto, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CollectUserAction"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  stats.service.collector.UserActionProto.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new UserActionControllerMethodDescriptorSupplier("CollectUserAction"))
              .build();
        }
      }
    }
    return getCollectUserActionMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static UserActionControllerStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<UserActionControllerStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<UserActionControllerStub>() {
        @java.lang.Override
        public UserActionControllerStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new UserActionControllerStub(channel, callOptions);
        }
      };
    return UserActionControllerStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static UserActionControllerBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<UserActionControllerBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<UserActionControllerBlockingStub>() {
        @java.lang.Override
        public UserActionControllerBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new UserActionControllerBlockingStub(channel, callOptions);
        }
      };
    return UserActionControllerBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static UserActionControllerFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<UserActionControllerFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<UserActionControllerFutureStub>() {
        @java.lang.Override
        public UserActionControllerFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new UserActionControllerFutureStub(channel, callOptions);
        }
      };
    return UserActionControllerFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void collectUserAction(stats.service.collector.UserActionProto request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCollectUserActionMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service UserActionController.
   */
  public static abstract class UserActionControllerImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return UserActionControllerGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service UserActionController.
   */
  public static final class UserActionControllerStub
      extends io.grpc.stub.AbstractAsyncStub<UserActionControllerStub> {
    private UserActionControllerStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UserActionControllerStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new UserActionControllerStub(channel, callOptions);
    }

    /**
     */
    public void collectUserAction(stats.service.collector.UserActionProto request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCollectUserActionMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service UserActionController.
   */
  public static final class UserActionControllerBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<UserActionControllerBlockingStub> {
    private UserActionControllerBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UserActionControllerBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new UserActionControllerBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.google.protobuf.Empty collectUserAction(stats.service.collector.UserActionProto request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCollectUserActionMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service UserActionController.
   */
  public static final class UserActionControllerFutureStub
      extends io.grpc.stub.AbstractFutureStub<UserActionControllerFutureStub> {
    private UserActionControllerFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UserActionControllerFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new UserActionControllerFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> collectUserAction(
        stats.service.collector.UserActionProto request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCollectUserActionMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_COLLECT_USER_ACTION = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_COLLECT_USER_ACTION:
          serviceImpl.collectUserAction((stats.service.collector.UserActionProto) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getCollectUserActionMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              stats.service.collector.UserActionProto,
              com.google.protobuf.Empty>(
                service, METHODID_COLLECT_USER_ACTION)))
        .build();
  }

  private static abstract class UserActionControllerBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    UserActionControllerBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return stats.service.collector.CollectorProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("UserActionController");
    }
  }

  private static final class UserActionControllerFileDescriptorSupplier
      extends UserActionControllerBaseDescriptorSupplier {
    UserActionControllerFileDescriptorSupplier() {}
  }

  private static final class UserActionControllerMethodDescriptorSupplier
      extends UserActionControllerBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    UserActionControllerMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (UserActionControllerGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new UserActionControllerFileDescriptorSupplier())
              .addMethod(getCollectUserActionMethod())
              .build();
        }
      }
    }
    return result;
  }
}
