package se.laz.casual.jca.inbound.handler.service.casual.discovery

import spock.lang.Specification

import javax.naming.InitialContext
import javax.naming.NameClassPair
import javax.naming.NamingEnumeration
import javax.naming.NamingException
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.logging.Logger

class JndiUtilTest extends Specification
{
   def 'lookup throws NamingException'()
   {
      given:
      String name = 'will throw'
      String outerName = 'outer'
      NamingEnumeration<NameClassPair> outerItems = Mock{
         2 * hasMoreElements() >>> [true, false]
         1 * next() >> {
            return new NameClassPair(outerName, 'whatever')
         }
      }
      NamingEnumeration<NameClassPair> innerItems = Mock{
         2 * hasMoreElements() >>> [true, false]
         1 * next() >> {
            return new NameClassPair(name, 'whatever')
         }
      }

      InitialContext innerContext = Mock{
         1 * list(JndiUtil.CURRENT_CONTEXT) >> {
            return innerItems
         }
         1 * lookup(name) >> {
            throw new NamingException()
         }
      }

      InitialContext context = Mock {
         1 * list(JndiUtil.JAVA_GLOBAL_CONTEXT) >> {
            return outerItems
         }
         1 * lookup(JndiUtil.JAVA_GLOBAL_CONTEXT + outerName) >> {
            return innerContext
         }
      }
      Logger logger = Mock{
         1 * warning(_)
      }
      setMockLogger(logger)
      when:
      JndiUtil.findAllGlobalJndiProxies(context)
      then:
      noExceptionThrown()
   }

   def setMockLogger(mockLogger)
   {
      Field field = JndiUtil.getDeclaredField("logger")
      field.setAccessible(true)
      Field modifiers = Field.class.getDeclaredField("modifiers")
      modifiers.setAccessible(true);
      modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL)
      field.set(null, mockLogger)
   }
}
