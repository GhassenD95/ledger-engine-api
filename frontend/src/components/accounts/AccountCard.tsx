import { Link } from 'react-router-dom'
import type { Account } from '../../types'
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../ui/card'
import { Badge } from '../ui/badge'

const currencyFlags: Record<string, string> = {
  USD: '\u{1F1FA}\u{1F1F8}',
  EUR: '\u{1F1EA}\u{1F1FA}',
  GBP: '\u{1F1EC}\u{1F1E7}',
  TND: '\u{1F1F9}\u{1F1F3}',
}

function formatBalance(amount: number, currency: string) {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
    minimumFractionDigits: 2,
  }).format(amount)
}

export function AccountCard({ account }: { account: Account }) {
  const isPositive = account.balance >= 0
  const flag = currencyFlags[account.currency] || '\u{1F310}'

  return (
    <Link to={`/accounts/${account.id}`} className="block transition-all duration-200 hover:-translate-y-1 hover:shadow-lg">
      <Card className="h-full overflow-hidden group cursor-pointer">
        <CardHeader className="pb-3">
          <div className="flex items-start justify-between">
            <div className="flex items-center gap-2">
              <span className="text-2xl">{flag}</span>
              <div>
                <CardTitle className="text-lg">{account.ownerName}</CardTitle>
                <CardDescription>{account.currency} Account</CardDescription>
              </div>
            </div>
            <Badge variant={isPositive ? 'success' : 'destructive'}>
              {isPositive ? 'Active' : 'Negative'}
            </Badge>
          </div>
        </CardHeader>
        <CardContent>
          <div className="flex items-baseline gap-1">
            <span className="text-3xl font-bold tracking-tight">
              {formatBalance(account.balance, account.currency)}
            </span>
            <span className="text-sm text-muted-foreground">{account.currency}</span>
          </div>
          <div className="mt-3 h-1 w-full rounded-full bg-muted overflow-hidden">
            <div
              className={`h-full rounded-full transition-all duration-500 ${
                isPositive ? 'bg-emerald-500' : 'bg-red-500'
              }`}
              style={{ width: `${Math.min(Math.abs(account.balance) / 20, 100)}%` }}
            />
          </div>
        </CardContent>
      </Card>
    </Link>
  )
}
