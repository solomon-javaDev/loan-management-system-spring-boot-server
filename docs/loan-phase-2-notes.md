# Loan phase 2 implementation notes

## Loan schedule approach
- The current implementation uses a flat-rate installment schedule.
- Interest is calculated once as a simple flat charge on the principal for the selected tenor.
- Each installment receives an equal principal portion and an equal interest portion.
- Repayments are applied per installment and overpayment is blocked.

## Supported workflow
1. Create a loan with customer, officer, guarantor, amount, rate, and tenor.
2. The system generates installments and stores balances on the loan.
3. Repayments can be applied to a specific installment.
4. A cashier session can be opened and closed, and cash transactions can be recorded.
