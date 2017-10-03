/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package se.kodarkatten.casual.jca;

import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.security.auth.Subject;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

/**
 * CasualManagedConnectionFactory
 *
 * @version $Revision: $
 */
@ConnectionDefinition(connectionFactory = CasualConnectionFactory.class,
   connectionFactoryImpl = CasualConnectionFactoryImpl.class,
   connection = CasualConnection.class,
   connectionImpl = CasualConnectionImpl.class)
public class CasualManagedConnectionFactory implements ManagedConnectionFactory, ResourceAdapterAssociation
{

   private static final long serialVersionUID = 1L;
   private static Logger log = Logger.getLogger(CasualManagedConnectionFactory.class.getName());
   private ResourceAdapter ra;
   private PrintWriter logwriter;

   private String hostName;
   private Integer portNumber;

   public String getHostName()
   {
      return hostName;
   }

   public void setHostName(String hostName)
   {
      this.hostName = hostName;
   }

   public Integer getPortNumber()
   {
      return portNumber;
   }

   public void setPortNumber(Integer portNumber)
   {
      this.portNumber = portNumber;
   }

   @Override
   public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException
   {
      log.finest("createConnectionFactory()");
      return new CasualConnectionFactoryImpl(this, cxManager);
   }

   @Override
   public Object createConnectionFactory() throws ResourceException
   {
      throw new ResourceException("This resource adapter doesn't support non-managed environments");
   }

   @Override
   public ManagedConnection createManagedConnection(Subject subject,
         ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      log.finest("createManagedConnection()");
      return new CasualManagedConnection(this, cxRequestInfo);
   }

   @Override
   @SuppressWarnings({"rawtypes","unchecked"})
   public ManagedConnection matchManagedConnections(Set connectionSet,
         Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      log.finest("matchManagedConnections()");
      return (ManagedConnection)connectionSet.stream()
              .filter( s -> s instanceof CasualManagedConnection )
              .findFirst( )
              .orElse( null );
   }

   @Override
   public PrintWriter getLogWriter() throws ResourceException
   {
      log.finest("getLogWriter()");
      return logwriter;
   }

   @Override
   public void setLogWriter(PrintWriter out) throws ResourceException
   {
      log.finest("setLogWriter()");
      logwriter = out;
   }

   @Override
   public ResourceAdapter getResourceAdapter()
   {
      log.finest("getResourceAdapter()");
      return ra;
   }

   @Override
   public void setResourceAdapter(ResourceAdapter ra)
   {
      log.finest("setResourceAdapter()");
      this.ra = ra;
   }

   @Override
   public boolean equals(Object o)
   {
      if (this == o)
      {
         return true;
      }
      if (o == null || getClass() != o.getClass())
      {
         return false;
      }
      CasualManagedConnectionFactory that = (CasualManagedConnectionFactory) o;
      return Objects.equals(ra, that.ra) &&
              Objects.equals(hostName, that.hostName) &&
              Objects.equals(portNumber, that.portNumber);
   }

   @Override
   public int hashCode()
   {
      return Objects.hash(ra, hostName, portNumber);
   }

   @Override
   public String toString()
   {
      return "CasualManagedConnectionFactory{" +
              "ra=" + ra +
              ", hostName='" + hostName + '\'' +
              ", portNumber=" + portNumber +
              '}';
   }
}
