import { useState, useRef, useEffect } from 'react'
import { Search, Loader2, Copy, Check } from 'lucide-react'
import { getTransaction } from '../../lib/api'
import type { JournalEntry } from '../../types'
import { Button } from '../ui/button'
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../ui/card'
import { Badge } from '../ui/badge'
import { Table, TableHeader, TableBody, TableRow, TableHead, TableCell } from '../ui/table'

export function TransactionLookup({ initialRef }: { initialRef?: string }) {
  const [refId, setRefId] = useState(initialRef ?? '')
  const [result, setResult] = useState<JournalEntry | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [copied, setCopied] = useState(false)
  const searched = useRef(false)

  useEffect(() => {
    if (initialRef && !searched.current) {
      searched.current = true
      handleSearch(initialRef)
    }
  }, [initialRef])

  const handleSearch = async (id?: string) => {
    const value = id ?? refId
    if (!value.trim()) return
    setLoading(true)
    setError(null)
    setResult(null)
    try {
      const data = await getTransaction(value.trim())
      setResult(data)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Transaction not found')
    } finally {
      setLoading(false)
    }
  }

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <Card className="max-w-2xl mx-auto">
      <CardHeader>
        <CardTitle>Transaction Lookup</CardTitle>
        <CardDescription>Find a journal entry by its reference ID</CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="flex gap-2">
          <input
            value={refId}
            onChange={e => setRefId(e.target.value)}
            placeholder="Enter reference ID..."
            className="flex h-9 flex-1 rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
            onKeyDown={e => e.key === 'Enter' && handleSearch()}
          />
          <Button onClick={() => handleSearch()} disabled={loading}>
            {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : <Search className="h-4 w-4" />}
            Search
          </Button>
        </div>

        {error && (
          <div className="rounded-lg bg-destructive/10 p-4 text-sm text-destructive">
            {error}
          </div>
        )}

        {result && (
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <Badge variant="success">Found</Badge>
              <code className="text-xs bg-muted px-2 py-1 rounded flex-1 truncate" title={result.referenceId}>
                {result.referenceId}
              </code>
              <button
                onClick={() => copyToClipboard(result.referenceId)}
                className="shrink-0 p-1 rounded hover:bg-muted transition-colors"
                title="Copy reference ID"
              >
                {copied ? <Check className="h-4 w-4 text-emerald-500" /> : <Copy className="h-4 w-4 text-muted-foreground" />}
              </button>
            </div>
            <p className="text-sm">{result.description}</p>
            <p className="text-xs text-muted-foreground">
              Created: {new Date(result.createdAt).toLocaleString()}
            </p>

            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Entry ID</TableHead>
                  <TableHead>Amount</TableHead>
                  <TableHead>Type</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {result.entries.map(e => (
                  <TableRow key={e.id}>
                    <TableCell className="font-mono text-xs">{e.id.slice(0, 8)}...</TableCell>
                    <TableCell className={e.amount < 0 ? 'text-red-600' : 'text-emerald-600'}>
                      {new Intl.NumberFormat('en-US', {
                        style: 'currency',
                        currency: 'USD',
                      }).format(Math.abs(e.amount))}
                    </TableCell>
                    <TableCell>
                      <Badge variant={e.amount < 0 ? 'destructive' : 'success'}>
                        {e.amount < 0 ? 'Debit' : 'Credit'}
                      </Badge>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
