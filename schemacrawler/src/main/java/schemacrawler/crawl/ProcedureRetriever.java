/* 
 *
 * SchemaCrawler
 * http://sourceforge.net/projects/schemacrawler
 * Copyright (c) 2000-2006, Sualeh Fatehi.
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

package schemacrawler.crawl;


import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import schemacrawler.schema.ProcedureColumnType;
import schemacrawler.schema.ProcedureType;
import schemacrawler.util.AlphabeticalSortComparator;

/**
 * SchemaRetriever uses database metadata to get the details about the schema.
 * 
 * @author sfatehi
 */
final class ProcedureRetriever
  extends AbstractRetriever
{

  private static final Logger LOGGER = Logger
    .getLogger(ProcedureRetriever.class.getName());

  /**
   * Constructs a SchemaCrawler object, from a connection.
   * 
   * @param connection
   *          An open database connection.
   * @param driverClassName
   *          Class name of the JDBC driver
   * @param schemaPatternString
   *          JDBC schema pattern, or null
   * @throws SQLException
   *           On a SQL exception
   */
  ProcedureRetriever(final RetrieverConnection retrieverConnection)
    throws SQLException
  {
    super(retrieverConnection);
  }

  /**
   * Retrieves procedure metadata according to the parameters specified. No
   * column metadata is retrieved, for reasons of efficiency.
   * 
   * @param pattern
   *          Procedure name pattern for table
   * @param useRegExpPattern
   *          True is the procedure name pattern is a regular expression; false
   *          if the procedure name pattern is the JDBC pattern
   * @return A list of tables in the database that match the pattern
   * @throws SQLException
   *           On a SQL exception
   */
  NamedObjectList retrieveProcedures(final boolean retrieveProcedures,
                                     final InclusionRule procedureInclusionRule)
    throws SQLException
  {

    final NamedObjectList procedures = new NamedObjectList(
        new AlphabeticalSortComparator());

    if (!retrieveProcedures)
    {
      return procedures;
    }

    final ResultSet results;

    // get tables
    results = getRetrieverConnection().getMetaData()
      .getProcedures(null, getRetrieverConnection().getSchemaPattern(), "%");
    try
    {
      results.setFetchSize(FETCHSIZE);
    }
    catch (final NullPointerException e)
    {
      // Need this catch for the JDBC/ ODBC driver
      LOGGER.log(Level.WARNING, "", e);
    }
    while (results.next())
    {
      final String schema = results.getString(PROCEDURE_SCHEMA);
      final String catalog = results.getString("PROCEDURE_CAT");
      final String procedureName = results.getString(PROCEDURE_NAME);
      final short procedureType = results.getShort(PROCEDURE_TYPE);
      final String remarks = results.getString(REMARKS);

      if (procedureInclusionRule.include(procedureName))
      {
        final MutableProcedure procedure = new MutableProcedure();
        procedure.setName(procedureName);
        procedure.setCatalogName(catalog);
        procedure.setSchemaName(schema);
        procedure.setType(ProcedureType.valueOf(procedureType));
        procedure.setRemarks(remarks);
        // add it to the list
        procedures.add(procedure);
      }
    }
    results.close();

    return procedures;

  }

  /**
   * Retrieves a list of columns from the database, for the table specified.
   * 
   * @param procedure
   *          Table for which data is required.
   * @throws SQLException
   *           On a SQL exception
   */
  void retrieveProcedureColumns(final MutableProcedure procedure,
                                final InclusionRule columnInclusionRule,
                                final NamedObjectList columnDataTypes)
    throws SQLException
  {

    final String procedureName = procedure.getName();
    final String schema = procedure.getSchemaName();

    final ResultSet results;

    results = getRetrieverConnection().getMetaData()
      .getProcedureColumns(getRetrieverConnection().getCatalog(),
                           schema,
                           procedureName,
                           null);
    int ordinalNumber = 0;
    while (results.next())
    {

      final String columnName = results.getString(COLUMN_NAME);
      final short columnType = results.getShort("COLUMN_TYPE");
      final int dataType = results.getInt(DATA_TYPE);
      final String typeName = results.getString(TYPE_NAME);
      final int length = results.getInt("LENGTH");
      final int precision = results.getInt("PRECISION");
      boolean isNullable = results.getShort(NULLABLE) == DatabaseMetaData.procedureNullable;
      final String remarks = results.getString(REMARKS);

      if (columnInclusionRule.include(columnName))
      {
        final MutableProcedureColumn column = new MutableProcedureColumn();
        column.setName(columnName);
        column.setParent(procedure);
        column.setOrdinalPosition(ordinalNumber++);
        column.setProcedureColumnType(ProcedureColumnType.valueOf(columnType));
        column.lookupAndSetDataType(dataType, typeName, columnDataTypes);
        column.setSize(length);
        column.setPrecision(precision);
        column.setNullable(isNullable);
        column.setRemarks(remarks);

        procedure.addColumn(column);
      }
    }
    results.close();

  }

}
