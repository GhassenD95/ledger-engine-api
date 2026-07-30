import { useNavigate } from 'react-router-dom'
import { TransferForm } from '../components/transfers/TransferForm'
import { useTransfer } from '../hooks/use-transfer'
import { useAccounts } from '../hooks/use-accounts'
import { Card, CardHeader, CardTitle, CardContent } from '../components/ui/card'
import { Badge } from '../components/ui/badge'
import { CheckCircle, XCircle, Loader2 } from 'lucide-react'

export function TransferPage() {
  const { accounts, loading: accountsLoading } = useAccounts()
  const { transfer, loading, result, error } = useTransfer()
  const navigate = useNavigate()

  const handleSubmit = async (data: {
    referenceId: string
    sourceAccountId: string
    destinationAccountId: string
    amount: number
    description: string
  }) => {
    try {
      await transfer(data)
    } catch {
      // error is set by hook
    }
  }

  return (
    <div className="max-w-4xl mx-auto p-6 space-y-6">
      <div className="space-y-2">
        <h1 className="text-3xl font-bold tracking-tight">Transfer Funds</h1>
        <p className="text-muted-foreground">
          Execute secure, double-entry transfers with idempotency guarantees
        </p>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        {accountsLoading ? (
          <div className="flex items-center justify-center h-64">
            <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
          </div>
        ) : (
          <TransferForm accounts={accounts} onSubmit={handleSubmit} loading={loading} />
        )}

        <div className="space-y-4">
          {error && (
            <Card className="border-red-200 dark:border-red-900">
              <CardHeader className="pb-3">
                <div className="flex items-center gap-2">
                  <XCircle className="h-5 w-5 text-destructive" />
                  <CardTitle className="text-base">Transfer Failed</CardTitle>
                </div>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-muted-foreground">{error}</p>
              </CardContent>
            </Card>
          )}

          {result && (
            <>
              <Card className="border-emerald-200 dark:border-emerald-900">
                <CardHeader className="pb-3">
                  <div className="flex items-center gap-2">
                    <CheckCircle className="h-5 w-5 text-emerald-600" />
                    <CardTitle className="text-base">Transfer Complete</CardTitle>
                    <Badge variant={result.status.includes('DUPLICATE') ? 'warning' : 'success'}>
                      {result.status}
                    </Badge>
                  </div>
                </CardHeader>
                <CardContent className="space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">Journal Entry</span>
                    <span className="font-mono text-xs">{result.journalEntryId.slice(0, 12)}...</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">Reference ID</span>
                    <span
                      className="font-mono text-xs cursor-pointer text-primary hover:underline"
                      onClick={() => navigate(`/transaction?ref=${result.referenceId}`)}
                      title={result.referenceId}
                    >
                      {result.referenceId}
                    </span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">Amount</span>
                    <span className="font-medium">
                      {new Intl.NumberFormat('en-US', {
                        style: 'currency',
                        currency: 'USD',
                      }).format(result.amount)}
                    </span>
                  </div>
                </CardContent>
              </Card>

              <div className="flex gap-2">
                <Badge
                  variant="outline"
                  className="cursor-pointer hover:bg-accent"
                  onClick={() => navigate(`/accounts/${result.sourceAccountId}`)}
                >
                  View Source Account
                </Badge>
                <Badge
                  variant="outline"
                  className="cursor-pointer hover:bg-accent"
                  onClick={() => navigate(`/accounts/${result.destinationAccountId}`)}
                >
                  View Destination Account
                </Badge>
              </div>
            </>
          )}

          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-base">How it works</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-sm text-muted-foreground">
              <div className="flex gap-2">
                <span className="font-medium text-foreground">1.</span>
                <span>Accounts are locked with <code className="bg-muted px-1 rounded text-xs">FOR UPDATE</code> in sorted ID order to prevent deadlocks.</span>
              </div>
              <div className="flex gap-2">
                <span className="font-medium text-foreground">2.</span>
                <span>An immutable debit and credit entry is recorded in a single database transaction.</span>
              </div>
              <div className="flex gap-2">
                <span className="font-medium text-foreground">3.</span>
                <span>An outbox event is enqueued for asynchronous delivery to downstream services.</span>
              </div>
              <div className="flex gap-2">
                <span className="font-medium text-foreground">4.</span>
                <span>Duplicate requests are rejected via <code className="bg-muted px-1 rounded text-xs">referenceId</code> idempotency.</span>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
