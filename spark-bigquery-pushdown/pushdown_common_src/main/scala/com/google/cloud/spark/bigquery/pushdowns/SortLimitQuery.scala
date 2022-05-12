/*
 * Copyright 2022 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.spark.bigquery.pushdowns

import com.google.cloud.spark.bigquery.pushdowns.SparkBigQueryPushdownUtil.makeStatement
import org.apache.spark.sql.catalyst.expressions.Expression

/** Query for Sort and Limit operations.
 *
 * @constructor
 * @param limit   Limit expression.
 * @param orderBy Order By expressions.
 * @param child   The child node.
 * @param alias   Query alias.
 */
case class SortLimitQuery(
   expressionConverter: SparkExpressionConverter,
   expressionFactory: SparkExpressionFactory,
   limit: Option[Expression],
   orderBy: Seq[Expression],
   child: BigQuerySQLQuery,
   alias: String)
  extends BigQuerySQLQuery(
    expressionConverter,
    expressionFactory,
    alias,
    children = Seq(child)) {

  /** Builds the ORDER BY clause of the sort query and/or LIMIT clause of the sort query */
  override val suffixStatement: BigQuerySQLStatement = {
    val statementFirstPart =
      if (orderBy.nonEmpty) {
        ConstantString("ORDER BY") + makeStatement(
          orderBy.map(expressionToStatement),
          ","
        )
      } else {
        EmptyBigQuerySQLStatement()
      }

    statementFirstPart + limit
      .map(ConstantString("LIMIT") + expressionToStatement(_))
      .getOrElse(EmptyBigQuerySQLStatement())
  }
}
