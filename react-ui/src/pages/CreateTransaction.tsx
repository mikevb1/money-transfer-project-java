import { useState } from "react";
import ToastManager from "../utils/toastManager";

export function CreateTransaction() {
  const [fromAccount, setFromAccount] = useState("");
  const [toAccount, setToAccount] = useState("");
  const [amountToTransfer, setAmountToTransfer] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      // use env variable for the backend url
      const response = await fetch(
        `${
          import.meta.env.VITE_APP_BACKEND_URL
        }/api/transactions/requestTransaction`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            fromAccount: fromAccount,
            toAccount: toAccount,
            amountToTransfer: parseFloat(amountToTransfer),
          }),
        }
      );

      if (response.ok) {
        ToastManager.success("Transactie succesvol aangemaakt!");
        setFromAccount("");
        setToAccount("");
        setAmountToTransfer("");
      } else {
        ToastManager.error(
          "Er is een fout opgetreden bij het aanmaken van de transactie"
        );
      }
    } catch (error) {
      console.error("Error:", error);
      ToastManager.error(
        "Er is een fout opgetreden bij het aanmaken van de transactie"
      );
    }
  };

  const handleAmountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    // Sta alleen getallen toe met maximaal 2 decimalen
    if (value === "" || /^\d+\.?\d{0,2}$/.test(value)) {
      setAmountToTransfer(value);
    }
  };

  return (
    <div className="bg-white shadow sm:rounded-lg max-w-xl mx-auto">
      <div className="px-4 py-5 sm:p-6">
        <h3 className="text-lg font-medium leading-6 text-gray-900 mb-6">
          Nieuwe Transactie Aanmaken
        </h3>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label className="block text-sm font-medium text-gray-700">
              Van Rekening
            </label>
            <div className="mt-1">
              <input
                type="text"
                value={fromAccount}
                onChange={(e) => setFromAccount(e.target.value)}
                required
                className="shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md"
                placeholder="NL00 BANK 0123 4567 89"
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">
              Naar Rekening
            </label>
            <div className="mt-1">
              <input
                type="text"
                value={toAccount}
                onChange={(e) => setToAccount(e.target.value)}
                required
                className="shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md"
                placeholder="NL00 BANK 0123 4567 89"
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">
              Bedrag
            </label>
            <div className="mt-1 relative rounded-md shadow-sm">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <span className="text-gray-500 sm:text-sm">€</span>
              </div>
              <input
                type="text"
                value={amountToTransfer}
                onChange={handleAmountChange}
                required
                pattern="^\d+\.?\d{0,2}$"
                inputMode="decimal"
                placeholder="0.00"
                className="focus:ring-blue-500 focus:border-blue-500 block w-full pl-7 pr-12 sm:text-sm border-gray-300 rounded-md"
              />
            </div>
          </div>

          <div>
            <button
              type="submit"
              className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              Transactie Aanmaken
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
