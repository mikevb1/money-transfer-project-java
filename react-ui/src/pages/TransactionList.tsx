import { useState, useEffect } from "react";
import ToastManager from "../utils/toastManager";
import { translateStatus } from "../utils/statusTranslations";

interface Transaction {
  id: string;
  transactionReference: string;
  runId: string;
  workflowId: string;
  fromAccount: string;
  toAccount: string;
  amountToTransfer: number;
  createdAt: string;
  processedAt: string;
  status: "PENDING" | "APPROVED" | "DECLINED" | "IN_PROGRESS";
}

const mockTransactions: Transaction[] = [
  {
    id: "1",
    transactionReference: "1",
    runId: "1",
    workflowId: "1",
    fromAccount: "NL91 ABCD 0123 4567 89",
    toAccount: "NL91 WXYZ 9876 5432 10",
    amountToTransfer: 150.5,
    status: "PENDING",
    createdAt: "2024-01-01 12:00:00",
    processedAt: "2024-01-01 12:00:00",
  },
  {
    id: "2",
    transactionReference: "2",
    runId: "2",
    workflowId: "2",
    fromAccount: "NL91 EFGH 2468 1357 90",
    toAccount: "NL91 VWXY 8642 9731 11",
    amountToTransfer: 75.25,
    status: "APPROVED",
    createdAt: "2024-01-01 15:01:00",
    processedAt: "2024-01-01 16:02:00",
  },
  {
    id: "3",
    transactionReference: "3",
    runId: "3",
    workflowId: "3",
    fromAccount: "NL91 IJKL 1111 2222 33",
    toAccount: "NL91 RSTU 3333 4444 55",
    amountToTransfer: 250.0,
    status: "DECLINED",
    createdAt: "2024-01-01 13:01:00",
    processedAt: "2024-01-01 14:02:00",
  },
];

export function TransactionList() {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [useMockData, setUseMockData] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isRefreshDisabled, setIsRefreshDisabled] = useState(false);

  const fetchTransactions = async () => {
    setIsLoading(true);
    setError(null);
    console.log("Backend URL:", import.meta.env.VITE_APP_BACKEND_URL); // Debug log

    try {
      if (useMockData) {
        setTransactions(mockTransactions);
      } else {
        const response = await fetch(
          `${import.meta.env.VITE_APP_BACKEND_URL}/transactions`
        );
        if (!response.ok) {
          throw new Error("Kon geen verbinding maken met de server");
        }
        const data = await response.json();
        setTransactions(data);
      }
    } catch (error) {
      setError(
        error instanceof Error ? error.message : "Er is een fout opgetreden"
      );
      console.error("Error:", error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchTransactions();
  }, [useMockData]); // Herlaad wanneer useMockData verandert

  const handleApprove = async (transactionReference: string) => {
    if (useMockData) {
      setTransactions(
        transactions.map((t) =>
          t.transactionReference === transactionReference
            ? { ...t, status: "APPROVED" }
            : t
        )
      );
      ToastManager.success("Transactie succesvol goedgekeurd");
      return;
    }

    try {
      const response = await fetch(
        `${
          import.meta.env.VITE_APP_BACKEND_URL
        }/transactions/approve?transactionReference=${transactionReference}`,
        {
          method: "PUT",
        }
      );
      if (response.ok) {
        ToastManager.success("Transactie succesvol goedgekeurd");
        fetchTransactions();
      } else {
        ToastManager.error("Kon de transactie niet goedkeuren");
      }
    } catch (error) {
      console.error("Error:", error);
      ToastManager.error(
        "Er is een fout opgetreden bij het goedkeuren van de transactie"
      );
    }
  };

  const handleDecline = async (transactionReference: string) => {
    if (useMockData) {
      setTransactions(
        transactions.map((t) =>
          t.transactionReference === transactionReference
            ? { ...t, status: "DECLINED" }
            : t
        )
      );
      ToastManager.success("Transactie succesvol afgekeurd");
      return;
    }

    try {
      const response = await fetch(
        `${
          import.meta.env.VITE_APP_BACKEND_URL
        }/transactions/disapprove?transactionReference=${transactionReference}`,
        {
          method: "PUT",
        }
      );
      if (response.ok) {
        ToastManager.success("Transactie succesvol afgekeurd");
        fetchTransactions();
      } else {
        ToastManager.error("Kon de transactie niet afkeuren");
      }
    } catch (error) {
      console.error("Error:", error);
      ToastManager.error(
        "Er is een fout opgetreden bij het afkeuren van de transactie"
      );
    }
  };

  const handleRefresh = async () => {
    setIsRefreshDisabled(true);
    await fetchTransactions();

    // Enable de knop na 3 seconden
    setTimeout(() => {
      setIsRefreshDisabled(false);
    }, 3000);
  };

  return (
    <div className="bg-white shadow sm:rounded-lg overflow-hidden">
      <div className="px-4 py-5 sm:px-6 flex justify-between items-center">
        <h3 className="text-lg leading-6 font-medium text-gray-900">
          Transacties Overzicht
        </h3>
        <div className="flex space-x-4 items-center">
          <label className="inline-flex items-center">
            <input
              type="checkbox"
              checked={useMockData}
              onChange={(e) => setUseMockData(e.target.checked)}
              className="form-checkbox h-4 w-4 text-blue-600 transition duration-150 ease-in-out"
            />
            <span className="ml-2 text-sm text-gray-600">
              Gebruik demo data
            </span>
          </label>
          <button
            onClick={handleRefresh}
            disabled={isLoading || isRefreshDisabled}
            className="inline-flex items-center px-3 py-2 border border-transparent text-sm leading-4 font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isLoading || isRefreshDisabled ? (
              <svg
                className="animate-spin -ml-1 mr-2 h-4 w-4 text-white"
                fill="none"
                viewBox="0 0 24 24"
              >
                <circle
                  className="opacity-25"
                  cx="12"
                  cy="12"
                  r="10"
                  stroke="currentColor"
                  strokeWidth="4"
                />
                <path
                  className="opacity-75"
                  fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                />
              </svg>
            ) : (
              <svg
                className="h-4 w-4 mr-1"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
                />
              </svg>
            )}
            Ververs
          </button>
        </div>
      </div>

      {error && (
        <div className="px-4 py-3 bg-red-50 text-red-700 text-sm">
          <p>{error}</p>
        </div>
      )}

      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th
                scope="col"
                className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
              >
                Van
              </th>
              <th
                scope="col"
                className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
              >
                Naar
              </th>
              <th
                scope="col"
                className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
              >
                Bedrag
              </th>
              <th
                scope="col"
                className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
              >
                Aangemaakt
              </th>
              <th
                scope="col"
                className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
              >
                Verwerkt
              </th>
              <th
                scope="col"
                className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
              >
                Status
              </th>
              <th
                scope="col"
                className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
              >
                Acties
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {transactions.map((transaction) => (
              <tr key={transaction.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {transaction.fromAccount}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {transaction.toAccount}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  €{transaction.amountToTransfer.toFixed(2)}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {new Date(transaction.createdAt).toLocaleString("nl-NL")}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {transaction.processedAt
                    ? new Date(transaction.processedAt).toLocaleString("nl-NL")
                    : "-"}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span
                    className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                      transaction.status === "PENDING"
                        ? "bg-yellow-100 text-yellow-800"
                        : transaction.status === "APPROVED"
                        ? "bg-green-100 text-green-800"
                        : transaction.status === "IN_PROGRESS"
                        ? "bg-blue-100 text-blue-800"
                        : "bg-red-100 text-red-800"
                    }`}
                  >
                    {translateStatus(transaction.status)}
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                  {transaction.status === "PENDING" && (
                    <div className="flex space-x-2">
                      <button
                        onClick={() =>
                          handleApprove(transaction.transactionReference)
                        }
                        className="text-white bg-green-600 hover:bg-green-700 px-3 py-1 rounded-md text-sm"
                      >
                        Goedkeuren
                      </button>
                      <button
                        onClick={() =>
                          handleDecline(transaction.transactionReference)
                        }
                        className="text-white bg-red-600 hover:bg-red-700 px-3 py-1 rounded-md text-sm"
                      >
                        Afkeuren
                      </button>
                    </div>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
