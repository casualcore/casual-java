package se.laz.casual.jca.inbound.handler.service.casual.discovery

import spock.lang.Specification

import javax.naming.InitialContext
import javax.naming.NameClassPair
import javax.naming.NamingEnumeration
import javax.naming.NamingException

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
      when:
      JndiUtil.findAllGlobalJndiProxies(context)
      then:
      noExceptionThrown()
   }


}
