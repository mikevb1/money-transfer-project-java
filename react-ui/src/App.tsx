import { BrowserRouter, Routes, Route, Link } from "react-router-dom";
import { CreateTransaction } from "./pages/CreateTransaction";
import { TransactionList } from "./pages/TransactionList";
import { Toaster } from "react-hot-toast";
import { Footer } from "./components/Footer";

function App() {
  return (
    <>
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 5000,
          style: {
            minWidth: "250px",
          },
        }}
        containerStyle={{
          top: 20,
          right: 20,
        }}
        gutter={8}
        reverseOrder={false}
      />
      <div className="flex flex-col min-h-screen bg-gray-50">
        <BrowserRouter>
          <nav className="bg-white shadow-sm">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
              <div className="flex justify-between h-16">
                <div className="flex w-full">
                  <div className="flex justify-between w-full">
                    <Link
                      to="/create"
                      className="inline-flex items-center px-1 pt-1 text-sm font-medium text-gray-900 border-b-2 border-transparent hover:border-blue-500"
                    >
                      Nieuwe Transactie
                    </Link>
                    <Link
                      to="/transactions"
                      className="inline-flex items-center px-1 pt-1 text-sm font-medium text-gray-900 border-b-2 border-transparent hover:border-blue-500"
                    >
                      Transacties Overzicht
                    </Link>
                  </div>
                </div>
              </div>
            </div>
          </nav>

          <main className="flex-1 w-full max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
            <Routes>
              <Route path="/create" element={<CreateTransaction />} />
              <Route path="/transactions" element={<TransactionList />} />
              <Route path="/" element={<CreateTransaction />} />
            </Routes>
          </main>

          <Footer />
        </BrowserRouter>
      </div>
    </>
  );
}

export default App;
