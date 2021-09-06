/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services

import domain._
import models.responses
import models.responses._

class DomainService {

  def toDomainSummary(gtd: GuaranteeTransactionDeclaration): GuaranteeTransaction = {
    GuaranteeTransaction(gtd.postingDate, gtd.declarationID, gtd.defAmounts.openAmount, gtd.declarantsRefNumber,
      gtd.declarantsEORINumber, gtd.importersEORINumber, gtd.defAmounts.totalAmount, gtd.defAmounts.clearedAmount, None, None, Nil)
  }


  def toDomainSummary(cashTransactionsResponseDetail: CashTransactionsResponseDetail): domain.CashTransactions = {
    CashTransactions(
      cashTransactionsResponseDetail.pendingTransactions.map(_.declarations.map(pt => toDomain(pt.declaration))).getOrElse(Seq.empty),
      cashTransactionsResponseDetail.dailyStatements.map(_.map(ds => toDomain(ds.dailyStatement))).getOrElse(Seq.empty))
  }

  private def toDomain(declaration: DeclarationDetail): domain.Declaration = {
    Declaration(declaration.declarationID, declaration.declarantEORINumber, declaration.declarantReference, declaration.postingDate, declaration.amount, Nil)
  }

  def toDomainDetail(cashTransactionsResponseDetail: CashTransactionsResponseDetail): domain.CashTransactions = {
    CashTransactions(
      cashTransactionsResponseDetail.pendingTransactions.map(_.declarations.map(pt => toDomain(pt.declaration))).getOrElse(Seq.empty),
      cashTransactionsResponseDetail.dailyStatements.map(_.map(ds => toDomainDetail(ds.dailyStatement))).getOrElse(Seq.empty))
  }

  private def toDomain(dailyStatementDetail: DailyStatementDetail): domain.CashDailyStatement = {
    CashDailyStatement(dailyStatementDetail.date,
      dailyStatementDetail.openingBalance,
      dailyStatementDetail.closingBalance,
      dailyStatementDetail.declarations.map(_.map(d => toDomain(d.declaration))).getOrElse(Seq.empty),
      dailyStatementDetail.paymentsAndWithdrawals.map(_.map(pw => toDomain(pw.paymentAndWithdrawal))).getOrElse(Seq.empty))
  }

  def toDomainDetail(dailyStatementDetail: DailyStatementDetail): domain.CashDailyStatement = {
    CashDailyStatement(dailyStatementDetail.date,
      dailyStatementDetail.openingBalance,
      dailyStatementDetail.closingBalance,
      dailyStatementDetail.declarations.map(_.map(d => toDomainDetail(d.declaration))).getOrElse(Seq.empty),
      dailyStatementDetail.paymentsAndWithdrawals.map(_.map(pw => toDomain(pw.paymentAndWithdrawal))).getOrElse(Seq.empty))
  }



  def toDomainDetail(declaration: DeclarationDetail): domain.Declaration = {
    Declaration(declaration.declarationID, declaration.declarantEORINumber, declaration.declarantReference, declaration.postingDate, declaration.amount,
      declaration.taxGroups.map(container => toDomain(container.taxGroup)))
  }

  private def toDomain(taxGroupDetail: TaxGroupDetail): domain.TaxGroup = {
    TaxGroup(taxGroupDetail.taxGroupDescription, taxGroupDetail.amount)
  }


  private def toDomain(tt: responses.TaxType): domain.TaxType = {
    domain.TaxType(tt.taxType, toDomain(tt.defAmounts))
  }

  private def toDomain(ttg: responses.TaxTypeGroup): domain.TaxTypeGroup = {
    domain.TaxTypeGroup(ttg.taxTypeGroup, toDomain(ttg.defAmounts), toDomain(ttg.taxTypes.head))
  }

  private def toDomain(amounts: DefAmounts): domain.Amounts = {
    Amounts(amounts.openAmount, amounts.totalAmount, amounts.clearedAmount, amounts.updateDate)
  }

  private def toDomain(dd: responses.DueDate): domain.DueDate = {
    domain.DueDate(dd.dueDate, dd.reasonForSecurity, toDomain(dd.defAmounts), dd.taxTypeGroups.map(toDomain))
  }

  private def toDomain(paymentAndWithdrawalDetail: PaymentAndWithdrawalDetail): domain.Transaction = {
    Transaction(paymentAndWithdrawalDetail.amount,
      paymentAndWithdrawalDetail.`type`,
      paymentAndWithdrawalDetail.bankAccount)
  }

  def toDomainDetail(gtd: GuaranteeTransactionDeclaration): GuaranteeTransaction = {
    GuaranteeTransaction(
      gtd.postingDate,
      gtd.declarationID,
      gtd.defAmounts.openAmount,
      gtd.declarantsRefNumber,
      gtd.declarantsEORINumber,
      gtd.importersEORINumber,
      gtd.defAmounts.totalAmount,
      gtd.defAmounts.clearedAmount,
      gtd.interestCharge,
      gtd.c18Reference,
      gtd.dueDates.map(toDomain)
    )
  }

}
