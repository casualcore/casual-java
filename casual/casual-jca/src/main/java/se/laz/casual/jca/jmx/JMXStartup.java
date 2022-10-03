package se.laz.casual.jca.jmx;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.logging.Logger;

public class JMXStartup
{
   private static final Logger LOG = Logger.getLogger(JMXStartup.class.getName());
   private static final String NAME = "se.laz.casual.jca:type=Casual";
   private static final JMXStartup instance = new JMXStartup();

   public static JMXStartup getInstance()
   {
      return instance;
   }

   public void initJMX()
   {
      try
      {
         MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
         ObjectName name = new ObjectName(NAME);
         unregister(mbs, name);
         Casual mbean = new Casual();
         mbs.registerMBean(mbean, name);
      }
      catch (MalformedObjectNameException | NotCompliantMBeanException | InstanceAlreadyExistsException | MBeanRegistrationException e)
      {
         LOG.warning(() -> "CasualMBean initiation failed, JMX entry will not exist: " + e);
      }
   }

   private void unregister(MBeanServer server, ObjectName name) throws MBeanRegistrationException
   {
      try
      {
         server.unregisterMBean(name);
      }
      catch (InstanceNotFoundException e)
      {
         // Don't care.
      }
   }

}
