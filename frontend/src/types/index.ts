export interface Account {
  id: string
  ownerName: string
  currency: string
  balance: number
  createdAt: string
}

export interface AccountEntry {
  id: string
  journalEntryId: string
  amount: number
  createdAt: string
}

export interface JournalEntry {
  id: string
  referenceId: string
  description: string
  createdAt: string
  entries: AccountEntry[]
}

export interface TransferRequest {
  referenceId: string
  sourceAccountId: string
  destinationAccountId: string
  amount: number
  description: string
}

export interface TransferResponse {
  journalEntryId: string
  referenceId: string
  sourceAccountId: string
  destinationAccountId: string
  amount: number
  status: string
  timestamp: string
}
