/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionDefinition;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
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
//Non serialisable or transient for ResourceAdapter and PrintWriter - this is as shown in Iron Jacamar so ignoring.
@SuppressWarnings("squid:S1948")
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
   private Long casualProtocolVersion = 1000L;
   private final int resourceId = CasualResourceManager.getInstance().getNextId();

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

   public Long getCasualProtocolVersion()
   {
      return casualProtocolVersion;
   }

   public CasualManagedConnectionFactory setCasualProtocolVersion(Long casualProtocolVersion)
   {
      Objects.requireNonNull(casualProtocolVersion, "casual protocol version can not be null!");
      this.casualProtocolVersion = casualProtocolVersion;
      return this;
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
      return new CasualManagedConnection(this);
   }

   @Override
   @SuppressWarnings({"rawtypes","unchecked"})
   public ManagedConnection matchManagedConnections(Set connectionSet,
         Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      log.finest("matchManagedConnections()");
      return (ManagedConnection)connectionSet.stream()
              .filter( s -> s instanceof CasualManagedConnection)
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
              ", hostName='" + hostName + '\'' +
              ", portNumber=" + portNumber +
              '}';
   }

   public int getResourceId()
   {
      return resourceId;
   }
}
