/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import se.laz.casual.network.ProtocolVersion;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.resource.spi.ResourceAllocationException;
import javax.security.auth.Subject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * CasualManagedConnectionFactory
 *
 * @version $Revision: $
 */
//Non serialisable or transient for ResourceAdapter and PrintWriter - this is as shown in Iron Jacamar so ignoring.
@SuppressWarnings("squid:S1948")
@Stateless
public class CasualManagedConnectionFactory implements ManagedConnectionFactory, ResourceAdapterAssociation
{
    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(CasualManagedConnectionFactory.class.getName());
    private  DomainHandler domainHandler;
    private ResourceAdapter ra;
    private PrintWriter logwriter;

   private String hostName;
   private Integer portNumber;
   private Long casualProtocolVersion = 1000L;
   private final int resourceId = CasualResourceManager.getInstance().getNextId();

   // for wls
   public CasualManagedConnectionFactory()
   {}

   @Inject
   public CasualManagedConnectionFactory(DomainHandler domainHandler)
   {
       this.domainHandler = domainHandler;
   }

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

   public ProtocolVersion getCasualProtocolVersion()
   {
      return ProtocolVersion.unmarshall(casualProtocolVersion);
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
       try
       {
           CasualManagedConnection managedConnection = new CasualManagedConnection(this);
           DomainId domainId = managedConnection.getDomainId();
           domainHandler.addDomainId(getAddress(), domainId);
           return managedConnection;
       }
       catch(Exception e)
       {
           StringWriter writer = new StringWriter();
           PrintWriter printWriter = new PrintWriter( writer );
           e.printStackTrace(printWriter);
           printWriter.flush();
           log.warning(() -> "createManagedConnection failed: " + writer);
           throw new ResourceAllocationException(e);
       }
   }

    @Override
   @SuppressWarnings({"rawtypes","unchecked"})
   public ManagedConnection matchManagedConnections(Set connectionSet,
         Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      log.finest("matchManagedConnections()");
      if( null != cxRequestInfo && cxRequestInfo instanceof CasualRequestInfo)
      {
          CasualRequestInfo requestInfo = (CasualRequestInfo) cxRequestInfo;
          // TODO: Why do we need to do this, streaming the Set and collecting returns not a List of objects but an Object???
          List<Object> wrapper = new ArrayList<>();
          wrapper.addAll(connectionSet);
          List<CasualManagedConnection> managedConnections = wrapper.stream()
                                                                    .filter(CasualManagedConnection.class::isInstance)
                                                                    .map(CasualManagedConnection.class::cast)
                                                                    .collect(Collectors.toList());
          ManagedConnection managedConnection = matchManagedConnections( managedConnections, requestInfo);
          if(null != managedConnection)
          {
              return managedConnection;
          }
      }
      return (ManagedConnection)connectionSet.stream()
              .filter( s -> s instanceof CasualManagedConnection)
              .findFirst( )
              .orElse( null );
   }

   public List<DomainId> getPoolDomainIds()
   {
       return Collections.unmodifiableList(domainHandler.getDomainIds(getAddress()));
   }

   public void domainDisconnect(DomainId domainId)
   {
       domainHandler.domainDisconnect(getAddress(), domainId);
   }

    private ManagedConnection matchManagedConnections(List<CasualManagedConnection> connections, CasualRequestInfo requestInfo)
    {
        DomainId domainId = requestInfo.getDomainId().orElse(null);
        if(null != domainId)
        {
            ManagedConnection matchedConnection =  connections.stream()
                                                              .filter(connection -> connection.getDomainId().equals(domainId))
                                                              .findFirst()
                                                              .orElse(null);
            log.finest(() -> null != matchedConnection ? "matchManagedConnections: " + domainId : "matchManagedConnections no match for: :" + domainId);
            return matchedConnection;
        }
        return null;
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

   public void addConnectionListener(CasualConnectionListener listener)
   {
       domainHandler.addConnectionListener(getAddress(), listener);
   }

   public void removeConnectionListener(CasualConnectionListener listener)
   {
       domainHandler.removeConnectionListener(getAddress(), listener);
   }

   private Address getAddress()
    {
        return Address.of(getHostName(), getPortNumber());
    }
}
