package se.laz.casual.network.protocol.utils

import se.laz.casual.api.network.protocol.messages.CasualNWMessageType
import se.laz.casual.network.ProtocolVersion

import java.nio.file.Files
import java.nio.file.Path

class CompleteMessageWriter
{
   static def headlessMessages = [new HeadlessCasualMessage(CasualNWMessageType.DOMAIN_CONNECT_REQUEST,
           'cHPL9BRESkGHswCG8UP8YDFdrMYYLkwSv5h376kky4YAAAAAAAAACGRvbWFpbiBBAAAAAAAAAAUAAAAAAAAD7AAAAAAAAAPrAAAAAAAAA+oAAAAAAAAD6QAAAAAAAAPo',
           ProtocolVersion.VERSION_1_0, 'message.gateway.domain.connect.request.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.DOMAIN_CONNECT_REPLY,
                                          'cHPL9BRESkGHswCG8UP8YDFdrMYYLkwSv5h376kky4YAAAAAAAAACGRvbWFpbiBBAAAAAAAAA+g=',
                                          ProtocolVersion.VERSION_1_0, 'message.gateway.domain.connect.reply.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.DOMAIN_DISCONNECT_REQUEST,
                                          'cHPL9BRESkGHswCG8UP8YA==',
                                          ProtocolVersion.VERSION_1_1, 'message.gateway.domain.disconnect.request.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.DOMAIN_DISCONNECT_REPLY,
                                          'cHPL9BRESkGHswCG8UP8YA==',
                                          ProtocolVersion.VERSION_1_1, 'message.gateway.domain.disconnect.reply.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.DOMAIN_DISCOVERY_REQUEST,
                                          'cHPL9BRESkGHswCG8UP8YDFdrMYYLkwSv5h376kky4YAAAAAAAAACGRvbWFpbiBBAAAAAAAAAAMAAAAAAAAACHNlcnZpY2UxAAAAAAAAAAhzZXJ2aWNlMgAAAAAAAAAIc2VydmljZTMAAAAAAAAAAwAAAAAAAAAGcXVldWUxAAAAAAAAAAZxdWV1ZTIAAAAAAAAABnF1ZXVlMw==',
                                          ProtocolVersion.VERSION_1_0, 'message.gateway.domain.discovery.request.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.DOMAIN_DISCOVERY_REPLY,
                                          'cHPL9BRESkGHswCG8UP8YOL2t8N/c0oJgqCrFYGyH6UAAAAAAAAACGRvbWFpbiBCAAAAAAAAAAEAAAAAAAAACHNlcnZpY2UxAAAAAAAAAAdleGFtcGxlAAEAAAAU9GsEAAAAAAAAAAAAAAAAAAAAAAEAAAAAAAAABnF1ZXVlMQAAAAAAAAAK',
                                          ProtocolVersion.VERSION_1_0, 'message.gateway.domain.discovery.reply.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.DOMAIN_DISCOVERY_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_FOUR,
                                          'cHPL9BRESkGHswCG8UP8YOL2t8N/c0oJgqCrFYGyH6UAAAAAAAAACGRvbWFpbiBCAAAAAAAAAAEAAAAAAAAACHNlcnZpY2UxAAAAAAAAAAdleGFtcGxlAAEAAAAU9GsEAAAAAAAAAAAAAAAAAAAAAAEAAAAAAAAABnF1ZXVlMQAAAAAAAAAKAAAAAAA9CQABAA==',
                                          ProtocolVersion.VERSION_1_4, 'message.gateway.domain.discovery.reply.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.DOMAIN_DISCOVERY_TOPOLOGY_UPDATE,
                                          'cHPL9BRESkGHswCG8UP8YAAAAAAAAAAB4fa3w39zSgmCoKsVgbIfogAAAAAAAAABQg==',
                                          ProtocolVersion.VERSION_1_2, 'message.gateway.domain.discovery.topology.update.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.SERVICE_CALL_REQUEST_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE,
                                          'cHPL9BRESkGHswCG8UP8YAAAAAAAAAAIc2VydmljZTEBAAAACcdlJACAgYKDhIWGhwAAAAAAAAAOcGFyZW50LXNlcnZpY2UAAAAAAAAAKgAAAAAAAAAQAAAAAAAAABBbbBv28ktIDb283vVMOghRW2wb9vJLSA29vN71TDoIUgAAAAAAAAAEAAAAAAAAAAguYmluYXJ5LwAAAAAAAACAgIGCg4SFhoeIiYqLjI2Oj5CRkpOUlZaXmJmam5ydnp+goaKjpKWmp6ipqqusra6vsLGys7S1tre4ubq7vL2+v8DBwsPExcbHyMnKy8zNzs/Q0dLT1NXW19jZ2tvc3d7f4OHi4+Tl5ufo6err7O3u7/Dx8vP09fb3+Pn6+/z9/v8=',
                                          ProtocolVersion.VERSION_1_3, 'message.service.call.request.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.SERVICE_CALL_REQUEST,
                                          'cHPL9BRESkGHswCG8UP8YAAAAAAAAAAIc2VydmljZTEAAAAJx2UkAAAAAAAAAAAOcGFyZW50LXNlcnZpY2UAAAAAAAAAKgAAAAAAAAAQAAAAAAAAABBbbBv28ktIDb283vVMOghRW2wb9vJLSA29vN71TDoIUgAAAAAAAAAEAAAAAAAAAAguYmluYXJ5LwAAAAAAAACAgIGCg4SFhoeIiYqLjI2Oj5CRkpOUlZaXmJmam5ydnp+goaKjpKWmp6ipqqusra6vsLGys7S1tre4ubq7vL2+v8DBwsPExcbHyMnKy8zNzs/Q0dLT1NXW19jZ2tvc3d7f4OHi4+Tl5ufo6err7O3u7/Dx8vP09fb3+Pn6+/z9/v8=',
                                          ProtocolVersion.VERSION_1_0, 'message.service.call.request.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.SERVICE_CALL_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE,
                                          'cHPL9BRESkGHswCG8UP8YAAAAAsAAAAAAAAAKgAAAAAAAAAACC5iaW5hcnkvAAAAAAAAAICAgYKDhIWGh4iJiouMjY6PkJGSk5SVlpeYmZqbnJ2en6ChoqOkpaanqKmqq6ytrq+wsbKztLW2t7i5uru8vb6/wMHCw8TFxsfIycrLzM3Oz9DR0tPU1dbX2Nna29zd3t/g4eLj5OXm5+jp6uvs7e7v8PHy8/T19vf4+fr7/P3+/w==',
                                          ProtocolVersion.VERSION_1_3, 'message.service.call.reply.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.SERVICE_CALL_REPLY,
                                          'cHPL9BRESkGHswCG8UP8YAAAAAsAAAAAAAAAKgAAAAAAAAAqAAAAAAAAABAAAAAAAAAAEFtsG/byS0gNvbze9Uw6CFFbbBv28ktIDb283vVMOghSAAAAAAAAAAAILmJpbmFyeS8AAAAAAAAAgICBgoOEhYaHiImKi4yNjo+QkZKTlJWWl5iZmpucnZ6foKGio6SlpqeoqaqrrK2ur7CxsrO0tba3uLm6u7y9vr/AwcLDxMXGx8jJysvMzc7P0NHS09TV1tfY2drb3N3e3+Dh4uPk5ebn6Onq6+zt7u/w8fLz9PX29/j5+vv8/f7/',
                                          ProtocolVersion.VERSION_1_0, 'message.service.call.reply.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.PREPARE_REQUEST,
                                          'cHPL9BRESkGHswCG8UP8YAAAAAAAAAAqAAAAAAAAABAAAAAAAAAAEFtsG/byS0gNvbze9Uw6CFFbbBv28ktIDb283vVMOghSAAAAKgAAAAAAAAAA',
                                          ProtocolVersion.VERSION_1_0, 'message.transaction.resource.prepare.request.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.PREPARE_REQUEST_REPLY,
                                          'cHPL9BRESkGHswCG8UP8YAAAAAAAAAAqAAAAAAAAABAAAAAAAAAAEFtsG/byS0gNvbze9Uw6CFFbbBv28ktIDb283vVMOghSAAAAKgAAAAA=',
                                          ProtocolVersion.VERSION_1_0, 'message.transaction.resource.prepare.reply.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.COMMIT_REQUEST,
                                          'cHPL9BRESkGHswCG8UP8YAAAAAAAAAAqAAAAAAAAABAAAAAAAAAAEFtsG/byS0gNvbze9Uw6CFFbbBv28ktIDb283vVMOghSAAAAKgAAAAAAAAAA',
                                          ProtocolVersion.VERSION_1_0, 'message.transaction.resource.commit.request.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.COMMIT_REQUEST_REPLY,
                                          'cHPL9BRESkGHswCG8UP8YAAAAAAAAAAqAAAAAAAAABAAAAAAAAAAEFtsG/byS0gNvbze9Uw6CFFbbBv28ktIDb283vVMOghSAAAAKgAAAAA=',
                                          ProtocolVersion.VERSION_1_0, 'message.transaction.resource.commit.reply.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.REQUEST_ROLLBACK,
                                          'cHPL9BRESkGHswCG8UP8YAAAAAAAAAAqAAAAAAAAABAAAAAAAAAAEFtsG/byS0gNvbze9Uw6CFFbbBv28ktIDb283vVMOghSAAAAKgAAAAAAAAAA',
                                          ProtocolVersion.VERSION_1_0, 'message.transaction.resource.rollback.request.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.REQUEST_ROLLBACK_REPLY,
                                          'cHPL9BRESkGHswCG8UP8YAAAAAAAAAAqAAAAAAAAABAAAAAAAAAAEFtsG/byS0gNvbze9Uw6CFFbbBv28ktIDb283vVMOghSAAAAKgAAAAA=',
                                          ProtocolVersion.VERSION_1_0, 'message.transaction.resource.rollback.reply.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.ENQUEUE_REQUEST,
                                          'cHPL9BRESkGHswCG8UP8YAAAAAAAAAAGcXVldWVBAAAAAAAAACoAAAAAAAAAEAAAAAAAAAAQW2wb9vJLSA29vN71TDoIUVtsG/byS0gNvbze9Uw6CFLm/Z/PhqxH9KUlL1l+JfxqAAAAAAAAABVwcm9wZXJ0eSAxOnByb3BlcnR5IDIAAAAAAAAABnF1ZXVlQhWlY3jTqfCgAAAAAAAAAAguYmluYXJ5LwAAAAAAAACAgIGCg4SFhoeIiYqLjI2Oj5CRkpOUlZaXmJmam5ydnp+goaKjpKWmp6ipqqusra6vsLGys7S1tre4ubq7vL2+v8DBwsPExcbHyMnKy8zNzs/Q0dLT1NXW19jZ2tvc3d7f4OHi4+Tl5ufo6err7O3u7/Dx8vP09fb3+Pn6+/z9/v8=',
                                          ProtocolVersion.VERSION_1_0, 'message.queue.enqueue.request.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.ENQUEUE_REPLY,
                                          'cHPL9BRESkGHswCG8UP8YDFdrMYYLkwSv5h376kky4c=',
                                          ProtocolVersion.VERSION_1_0, 'message.queue.enqueue.reply.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.ENQUEUE_REPLY_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE,
                                          'cHPL9BRESkGHswCG8UP8YByY2UvSmkBhnYpzABnciR8AAAAe',
                                          ProtocolVersion.VERSION_1_3, 'message.queue.enqueue.reply.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.DEQUEUE_REQUEST,
                                          'cHPL9BRESkGHswCG8UP8YAAAAAAAAAAGcXVldWVBAAAAAAAAACoAAAAAAAAAEAAAAAAAAAAQW2wb9vJLSA29vN71TDoIUVtsG/byS0gNvbze9Uw6CFIAAAAAAAAAFXByb3BlcnR5IDE6cHJvcGVydHkgMjFdrMYYLkwSv5h376kky4cA',
                                          ProtocolVersion.VERSION_1_0, 'message.queue.dequeue.request.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.DEQUEUE_REPLY,
                                          'cHPL9BRESkGHswCG8UP8YAAAAAAAAAABUy+LbBV2Tcqf6CowAt5XngAAAAAAAAAVcHJvcGVydHkgMTpwcm9wZXJ0eSAyAAAAAAAAAAZxdWV1ZUIVpWN406nwoAAAAAAAAAAGLmpzb24vAAAAAAAAAAJ7fQAAAAAAAAABFaVjeNOp8KA=',
                                          ProtocolVersion.VERSION_1_0, 'message.queue.dequeue.reply.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.DEQUEUE_REPLY_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE,
                                          'cHPL9BRESkGHswCG8UP8YAFTL4tsFXZNyp/oKjAC3leeAAAAAAAAABVwcm9wZXJ0eSAxOnByb3BlcnR5IDIAAAAAAAAABnF1ZXVlQhWlY3jTqfCgAAAAAAAAAAYuanNvbi8AAAAAAAAAAnt9AAAAAAAAAAEVpWN406nwoAAAABQ=',
                                          ProtocolVersion.VERSION_1_3, 'message.queue.dequeue.reply.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.CONVERSATION_CONNECT,
                                          'cHPL9BRESkGHswCG8UP8YAAAAAAAAAAIc2VydmljZTEAAAAJx2UkAAAAAAAAAAAOcGFyZW50LXNlcnZpY2UAAAAAAAAAKgAAAAAAAAAQAAAAAAAAABBbbBv28ktIDb283vVMOghRW2wb9vJLSA29vN71TDoIUgAAAAAAAAAAAAguYmluYXJ5LwAAAAAAAACAgIGCg4SFhoeIiYqLjI2Oj5CRkpOUlZaXmJmam5ydnp+goaKjpKWmp6ipqqusra6vsLGys7S1tre4ubq7vL2+v8DBwsPExcbHyMnKy8zNzs/Q0dLT1NXW19jZ2tvc3d7f4OHi4+Tl5ufo6err7O3u7/Dx8vP09fb3+Pn6+/z9/v8=',
                                          ProtocolVersion.VERSION_1_0, 'message.conversation.connect.request.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.CONVERSATION_CONNECT_PROTOCOL_VERSION_EQUAL_OR_GREATER_TO_ONE_THREE,
                                          'cHPL9BRESkGHswCG8UP8YAAAAAAAAAAIc2VydmljZTEBAAAACcdlJACAgYKDhIWGhwAAAAAAAAAOcGFyZW50LXNlcnZpY2UAAAAAAAAAKgAAAAAAAAAQAAAAAAAAABBbbBv28ktIDb283vVMOghRW2wb9vJLSA29vN71TDoIUgAAAAAAAAAAAAguYmluYXJ5LwAAAAAAAACAgIGCg4SFhoeIiYqLjI2Oj5CRkpOUlZaXmJmam5ydnp+goaKjpKWmp6ipqqusra6vsLGys7S1tre4ubq7vL2+v8DBwsPExcbHyMnKy8zNzs/Q0dLT1NXW19jZ2tvc3d7f4OHi4+Tl5ufo6err7O3u7/Dx8vP09fb3+Pn6+/z9/v8=',
                                          ProtocolVersion.VERSION_1_3, 'message.conversation.connect.request.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.CONVERSATION_CONNECT_REPLY,
                                          'cHPL9BRESkGHswCG8UP8YP////8=',
                                          ProtocolVersion.VERSION_1_0, 'message.conversation.connect.reply.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.CONVERSATION_REQUEST,
                                          'cHPL9BRESkGHswCG8UP8YAAAAAAAAAAAAAAAAAAqAAAAAAAAAAguYmluYXJ5LwAAAAAAAACAgIGCg4SFhoeIiYqLjI2Oj5CRkpOUlZaXmJmam5ydnp+goaKjpKWmp6ipqqusra6vsLGys7S1tre4ubq7vL2+v8DBwsPExcbHyMnKy8zNzs/Q0dLT1NXW19jZ2tvc3d7f4OHi4+Tl5ufo6err7O3u7/Dx8vP09fb3+Pn6+/z9/v8=',
                                          ProtocolVersion.VERSION_1_0, 'message.conversation.request.%d.%d.b64'),
                                  new HeadlessCasualMessage(CasualNWMessageType.CONVERSATION_DISCONNECT,
                                          'cHPL9BRESkGHswCG8UP8YA==',
                                          ProtocolVersion.VERSION_1_0, 'message.conversation.disconnect.%d.%d.b64'),



   ]
   static void createBase64Dumps(String path)
   {
      headlessMessages.forEach({m ->
         def filename = String.format(m.template,m.protocolVersion.getVersion(), m.type.messageId)
         dump(Path.of(path, filename),  m.completeMessageBase64())
      })
   }
   static void dump(def path, String base64)
   {
      println("writing file: ${path}")
      Files.writeString(path, base64)
   }
   static void main(String[] args)
   {
      try
      {
         String path = Files.createTempDirectory('casual-protocol').toFile().getAbsolutePath()
         createBase64Dumps(path)
         println("base64 dumps created in ${path}")
      }
      catch(Exception e)
      {
         println("Exception: ${e}")
      }

   }
}
