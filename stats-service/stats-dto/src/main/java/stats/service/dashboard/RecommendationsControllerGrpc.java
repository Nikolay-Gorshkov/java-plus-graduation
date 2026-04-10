package stats.service.dashboard;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.63.0)",
    comments = "Source: dashboard.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class RecommendationsControllerGrpc {

  private RecommendationsControllerGrpc() {}

  public static final java.lang.String SERVICE_NAME = "stats.service.dashboard.RecommendationsController";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<stats.service.dashboard.UserPredictionsRequestProto,
      stats.service.dashboard.RecommendedEventProto> getGetRecommendationsForUserMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetRecommendationsForUser",
      requestType = stats.service.dashboard.UserPredictionsRequestProto.class,
      responseType = stats.service.dashboard.RecommendedEventProto.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<stats.service.dashboard.UserPredictionsRequestProto,
      stats.service.dashboard.RecommendedEventProto> getGetRecommendationsForUserMethod() {
    io.grpc.MethodDescriptor<stats.service.dashboard.UserPredictionsRequestProto, stats.service.dashboard.RecommendedEventProto> getGetRecommendationsForUserMethod;
    if ((getGetRecommendationsForUserMethod = RecommendationsControllerGrpc.getGetRecommendationsForUserMethod) == null) {
      synchronized (RecommendationsControllerGrpc.class) {
        if ((getGetRecommendationsForUserMethod = RecommendationsControllerGrpc.getGetRecommendationsForUserMethod) == null) {
          RecommendationsControllerGrpc.getGetRecommendationsForUserMethod = getGetRecommendationsForUserMethod =
              io.grpc.MethodDescriptor.<stats.service.dashboard.UserPredictionsRequestProto, stats.service.dashboard.RecommendedEventProto>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetRecommendationsForUser"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  stats.service.dashboard.UserPredictionsRequestProto.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  stats.service.dashboard.RecommendedEventProto.getDefaultInstance()))
              .setSchemaDescriptor(new RecommendationsControllerMethodDescriptorSupplier("GetRecommendationsForUser"))
              .build();
        }
      }
    }
    return getGetRecommendationsForUserMethod;
  }

  private static volatile io.grpc.MethodDescriptor<stats.service.dashboard.SimilarEventsRequestProto,
      stats.service.dashboard.RecommendedEventProto> getGetSimilarEventsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetSimilarEvents",
      requestType = stats.service.dashboard.SimilarEventsRequestProto.class,
      responseType = stats.service.dashboard.RecommendedEventProto.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<stats.service.dashboard.SimilarEventsRequestProto,
      stats.service.dashboard.RecommendedEventProto> getGetSimilarEventsMethod() {
    io.grpc.MethodDescriptor<stats.service.dashboard.SimilarEventsRequestProto, stats.service.dashboard.RecommendedEventProto> getGetSimilarEventsMethod;
    if ((getGetSimilarEventsMethod = RecommendationsControllerGrpc.getGetSimilarEventsMethod) == null) {
      synchronized (RecommendationsControllerGrpc.class) {
        if ((getGetSimilarEventsMethod = RecommendationsControllerGrpc.getGetSimilarEventsMethod) == null) {
          RecommendationsControllerGrpc.getGetSimilarEventsMethod = getGetSimilarEventsMethod =
              io.grpc.MethodDescriptor.<stats.service.dashboard.SimilarEventsRequestProto, stats.service.dashboard.RecommendedEventProto>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetSimilarEvents"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  stats.service.dashboard.SimilarEventsRequestProto.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  stats.service.dashboard.RecommendedEventProto.getDefaultInstance()))
              .setSchemaDescriptor(new RecommendationsControllerMethodDescriptorSupplier("GetSimilarEvents"))
              .build();
        }
      }
    }
    return getGetSimilarEventsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<stats.service.dashboard.InteractionsCountRequestProto,
      stats.service.dashboard.RecommendedEventProto> getGetInteractionsCountMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetInteractionsCount",
      requestType = stats.service.dashboard.InteractionsCountRequestProto.class,
      responseType = stats.service.dashboard.RecommendedEventProto.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<stats.service.dashboard.InteractionsCountRequestProto,
      stats.service.dashboard.RecommendedEventProto> getGetInteractionsCountMethod() {
    io.grpc.MethodDescriptor<stats.service.dashboard.InteractionsCountRequestProto, stats.service.dashboard.RecommendedEventProto> getGetInteractionsCountMethod;
    if ((getGetInteractionsCountMethod = RecommendationsControllerGrpc.getGetInteractionsCountMethod) == null) {
      synchronized (RecommendationsControllerGrpc.class) {
        if ((getGetInteractionsCountMethod = RecommendationsControllerGrpc.getGetInteractionsCountMethod) == null) {
          RecommendationsControllerGrpc.getGetInteractionsCountMethod = getGetInteractionsCountMethod =
              io.grpc.MethodDescriptor.<stats.service.dashboard.InteractionsCountRequestProto, stats.service.dashboard.RecommendedEventProto>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetInteractionsCount"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  stats.service.dashboard.InteractionsCountRequestProto.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  stats.service.dashboard.RecommendedEventProto.getDefaultInstance()))
              .setSchemaDescriptor(new RecommendationsControllerMethodDescriptorSupplier("GetInteractionsCount"))
              .build();
        }
      }
    }
    return getGetInteractionsCountMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static RecommendationsControllerStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RecommendationsControllerStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RecommendationsControllerStub>() {
        @java.lang.Override
        public RecommendationsControllerStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RecommendationsControllerStub(channel, callOptions);
        }
      };
    return RecommendationsControllerStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static RecommendationsControllerBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RecommendationsControllerBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RecommendationsControllerBlockingStub>() {
        @java.lang.Override
        public RecommendationsControllerBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RecommendationsControllerBlockingStub(channel, callOptions);
        }
      };
    return RecommendationsControllerBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static RecommendationsControllerFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RecommendationsControllerFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RecommendationsControllerFutureStub>() {
        @java.lang.Override
        public RecommendationsControllerFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RecommendationsControllerFutureStub(channel, callOptions);
        }
      };
    return RecommendationsControllerFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void getRecommendationsForUser(stats.service.dashboard.UserPredictionsRequestProto request,
        io.grpc.stub.StreamObserver<stats.service.dashboard.RecommendedEventProto> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetRecommendationsForUserMethod(), responseObserver);
    }

    /**
     */
    default void getSimilarEvents(stats.service.dashboard.SimilarEventsRequestProto request,
        io.grpc.stub.StreamObserver<stats.service.dashboard.RecommendedEventProto> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetSimilarEventsMethod(), responseObserver);
    }

    /**
     */
    default void getInteractionsCount(stats.service.dashboard.InteractionsCountRequestProto request,
        io.grpc.stub.StreamObserver<stats.service.dashboard.RecommendedEventProto> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetInteractionsCountMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service RecommendationsController.
   */
  public static abstract class RecommendationsControllerImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return RecommendationsControllerGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service RecommendationsController.
   */
  public static final class RecommendationsControllerStub
      extends io.grpc.stub.AbstractAsyncStub<RecommendationsControllerStub> {
    private RecommendationsControllerStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RecommendationsControllerStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RecommendationsControllerStub(channel, callOptions);
    }

    /**
     */
    public void getRecommendationsForUser(stats.service.dashboard.UserPredictionsRequestProto request,
        io.grpc.stub.StreamObserver<stats.service.dashboard.RecommendedEventProto> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getGetRecommendationsForUserMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getSimilarEvents(stats.service.dashboard.SimilarEventsRequestProto request,
        io.grpc.stub.StreamObserver<stats.service.dashboard.RecommendedEventProto> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getGetSimilarEventsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getInteractionsCount(stats.service.dashboard.InteractionsCountRequestProto request,
        io.grpc.stub.StreamObserver<stats.service.dashboard.RecommendedEventProto> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getGetInteractionsCountMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service RecommendationsController.
   */
  public static final class RecommendationsControllerBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<RecommendationsControllerBlockingStub> {
    private RecommendationsControllerBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RecommendationsControllerBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RecommendationsControllerBlockingStub(channel, callOptions);
    }

    /**
     */
    public java.util.Iterator<stats.service.dashboard.RecommendedEventProto> getRecommendationsForUser(
        stats.service.dashboard.UserPredictionsRequestProto request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getGetRecommendationsForUserMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<stats.service.dashboard.RecommendedEventProto> getSimilarEvents(
        stats.service.dashboard.SimilarEventsRequestProto request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getGetSimilarEventsMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<stats.service.dashboard.RecommendedEventProto> getInteractionsCount(
        stats.service.dashboard.InteractionsCountRequestProto request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getGetInteractionsCountMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service RecommendationsController.
   */
  public static final class RecommendationsControllerFutureStub
      extends io.grpc.stub.AbstractFutureStub<RecommendationsControllerFutureStub> {
    private RecommendationsControllerFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RecommendationsControllerFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RecommendationsControllerFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_GET_RECOMMENDATIONS_FOR_USER = 0;
  private static final int METHODID_GET_SIMILAR_EVENTS = 1;
  private static final int METHODID_GET_INTERACTIONS_COUNT = 2;

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
        case METHODID_GET_RECOMMENDATIONS_FOR_USER:
          serviceImpl.getRecommendationsForUser((stats.service.dashboard.UserPredictionsRequestProto) request,
              (io.grpc.stub.StreamObserver<stats.service.dashboard.RecommendedEventProto>) responseObserver);
          break;
        case METHODID_GET_SIMILAR_EVENTS:
          serviceImpl.getSimilarEvents((stats.service.dashboard.SimilarEventsRequestProto) request,
              (io.grpc.stub.StreamObserver<stats.service.dashboard.RecommendedEventProto>) responseObserver);
          break;
        case METHODID_GET_INTERACTIONS_COUNT:
          serviceImpl.getInteractionsCount((stats.service.dashboard.InteractionsCountRequestProto) request,
              (io.grpc.stub.StreamObserver<stats.service.dashboard.RecommendedEventProto>) responseObserver);
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
          getGetRecommendationsForUserMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              stats.service.dashboard.UserPredictionsRequestProto,
              stats.service.dashboard.RecommendedEventProto>(
                service, METHODID_GET_RECOMMENDATIONS_FOR_USER)))
        .addMethod(
          getGetSimilarEventsMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              stats.service.dashboard.SimilarEventsRequestProto,
              stats.service.dashboard.RecommendedEventProto>(
                service, METHODID_GET_SIMILAR_EVENTS)))
        .addMethod(
          getGetInteractionsCountMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              stats.service.dashboard.InteractionsCountRequestProto,
              stats.service.dashboard.RecommendedEventProto>(
                service, METHODID_GET_INTERACTIONS_COUNT)))
        .build();
  }

  private static abstract class RecommendationsControllerBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    RecommendationsControllerBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return stats.service.dashboard.DashboardProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("RecommendationsController");
    }
  }

  private static final class RecommendationsControllerFileDescriptorSupplier
      extends RecommendationsControllerBaseDescriptorSupplier {
    RecommendationsControllerFileDescriptorSupplier() {}
  }

  private static final class RecommendationsControllerMethodDescriptorSupplier
      extends RecommendationsControllerBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    RecommendationsControllerMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (RecommendationsControllerGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new RecommendationsControllerFileDescriptorSupplier())
              .addMethod(getGetRecommendationsForUserMethod())
              .addMethod(getGetSimilarEventsMethod())
              .addMethod(getGetInteractionsCountMethod())
              .build();
        }
      }
    }
    return result;
  }
}
