import { version } from "../../package.json";

export function Footer() {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="w-full bg-white border-t border-gray-200">
      <div className="max-w-7xl mx-auto py-4 px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center text-sm text-gray-500">
          <div>
            © {currentYear} Money Transfer App. Alle rechten voorbehouden.
          </div>
          {/* // version limit after second dot */}
          <div>V{version.slice(0, version.lastIndexOf("."))}</div>
        </div>
      </div>
    </footer>
  );
}
