interface DatePickerProps {
  value: string;
  onChange: (e: { target: { name: string; value: string } }) => void;
  name: string;
  placeholder?: string;
  className?: string;
}

export const DatePicker = ({ value, onChange, name, placeholder = "DD/MM/YYYY", className = "" }: DatePickerProps) => {
  const handleDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const storageValue = e.target.value; // YYYY-MM-DD del input nativo
    onChange({ target: { name, value: storageValue } });
  };

  return (
    <div className={`relative ${className}`}>
      <input
        type="date"
        name={name}
        value={value || ""}
        onChange={handleDateChange}
        placeholder={placeholder}
        className="w-full px-4 py-3 pr-12 bg-gray-800 text-gray-100 border border-gray-700 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 hover:border-gray-600 placeholder:text-gray-500"
      />
    </div>
  );
};
