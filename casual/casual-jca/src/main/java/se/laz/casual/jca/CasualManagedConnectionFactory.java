/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import se.laz.casual.api.discovery.DiscoveryReturn;
import se.laz.casual.jca.discovery.CasualDiscoveryCaller;
import se.laz.casual.network.ProtocolVersion;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.security.auth.Subject;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * CasualManagedConnectionFactory
 *
 * @version $Revision: $
 */
//Non serialisable or transient for ResourceAdapter and PrintWriter - this is as shown in Iron Jacamar so ignoring.
@SuppressWarnings("squid:S1948")
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
      log.finest("createManagedConnection()");
      return new CasualManagedConnection(this);
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
              return null;
          }
      }
      return (ManagedConnection)connectionSet.stream()
              .filter( s -> s instanceof CasualManagedConnection)
              .findFirst( )
              .orElse( null );
   }

    private ManagedConnection matchManagedConnections(List<CasualManagedConnection> connections, CasualRequestInfo requestInfo)
    {
        DomainId domainId = requestInfo.getDomainId().orElse(null);
        if(null != domainId)
        {
            return connections.stream()
                              .filter(connection -> connection.getDomainId().equals(domainId))
                              .findFirst()
                              .orElse(null);
        }
        return matchManagedConnectionsUsingDomainDiscovery(connections, requestInfo);
    }

    private ManagedConnection matchManagedConnectionsUsingDomainDiscovery(List<CasualManagedConnection> connections, CasualRequestInfo requestInfo)
    {
        if(requestInfo.getServices().isEmpty() && requestInfo.getQueues().isEmpty())
        {
            return null;
        }
        for(CasualManagedConnection connection : connections)
        {
            if(successfulDomainDiscovery(connection, requestInfo))
            {
                return connection;
            }
        }
        return null;
    }

    private boolean successfulDomainDiscovery(CasualManagedConnection connection, CasualRequestInfo requestInfo)
    {
        CasualDiscoveryCaller discoveryCaller = CasualDiscoveryCaller.of(connection);
        DiscoveryReturn discoveryReturn = discoveryCaller.discover(UUID.randomUUID(), requestInfo.getServices(), requestInfo.getQueues());
        int numberOfServicesFound =  discoveryReturn.getServiceDetails().stream()
                                                    .map(entry -> entry.getName())
                                                    .collect(Collectors.toList()).size();
        int numberOfQueuesFound =  discoveryReturn.getQueueDetails().stream()
                                                    .map(entry -> entry.getName())
                                                    .collect(Collectors.toList()).size();
        return requestInfo.getServices().size() == numberOfServicesFound  && requestInfo.getQueues().size() == numberOfQueuesFound;
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
