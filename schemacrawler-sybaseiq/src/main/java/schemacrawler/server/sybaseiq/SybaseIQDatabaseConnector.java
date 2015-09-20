/*
 *
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2015, Sualeh Fatehi.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 */
package schemacrawler.server.sybaseiq;


import java.util.regex.Pattern;

import schemacrawler.schemacrawler.DatabaseSpecificOverrideOptionsBuilder;
import schemacrawler.tools.databaseconnector.DatabaseConnector;
import schemacrawler.tools.databaseconnector.DatabaseSystemConnector;
import schemacrawler.tools.options.DatabaseServerType;

public final class SybaseIQDatabaseConnector
  extends DatabaseConnector
{

  private static final DatabaseServerType SYBASEIQ_SERVER_TYPE = new DatabaseServerType("sybaseiq",
                                                                                        "SAP Sybase IQ");

  private static final class SybaseIQDatabaseSystemConnector
    extends DatabaseSystemConnector
  {
    private SybaseIQDatabaseSystemConnector(final String configResource,
                                            final String informationSchemaViewsResourceFolder)
    {
      super(SYBASEIQ_SERVER_TYPE, configResource,
            informationSchemaViewsResourceFolder);
    }

    @Override
    public DatabaseSpecificOverrideOptionsBuilder
      getDatabaseSpecificOverrideOptionsBuilder()
    {
      final DatabaseSpecificOverrideOptionsBuilder databaseSpecificOverrideOptionsBuilder = super.getDatabaseSpecificOverrideOptionsBuilder();
      databaseSpecificOverrideOptionsBuilder.doesNotSupportCatalogs();
      return databaseSpecificOverrideOptionsBuilder;
    }

  }

  public SybaseIQDatabaseConnector()
  {
    super(SYBASEIQ_SERVER_TYPE, "/help/Connections.sybaseiq.txt",
          new SybaseIQDatabaseSystemConnector("/schemacrawler-sybaseiq.config.properties",
                                              "/sybaseiq.information_schema"));
  }

  @Override
  protected Pattern getConnectionUrlPattern()
  {
    return Pattern.compile("jdbc:sybase:.*");
  }

}