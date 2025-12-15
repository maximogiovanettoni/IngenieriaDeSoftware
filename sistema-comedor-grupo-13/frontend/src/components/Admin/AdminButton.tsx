import type { ReactNode } from 'react';

interface AdminButtonProps {
  icon: ReactNode;
  title: string;
  description: string;
  color?: string;
  onClick?: () => void;
}

export default function AdminButton({ icon, title, description, color = 'rgba(52,58,64,0.3)', onClick }: AdminButtonProps) {
  return (
    <button
      onClick={onClick}
      className="relative bg-[rgba(38,38,38,0.3)] h-[62px] rounded-[10px] shadow-[0px_10px_15px_-3px_rgba(0,0,0,0.1),0px_4px_6px_-4px_rgba(0,0,0,0.1)] w-full hover:bg-[rgba(21,93,252,0.3)] transition-all border border-[#4a5565] hover:border-[rgba(21,93,252,0.5)]"
    >
      <div className="flex items-center gap-3 px-4 py-2.5">
        <div className={`relative shrink-0 size-[40px] rounded-full flex items-center justify-center`} style={{ backgroundColor: color }}>
          {icon}
        </div>
        <div className="flex flex-col items-start gap-0.5">
          <p className="font-['Arimo:Regular',_sans-serif] font-normal leading-[16px] text-[13px] text-nowrap text-white whitespace-pre">{title}</p>
          <p className="font-['Arimo:Regular',_sans-serif] font-normal leading-[14px] text-[#99a1af] text-[10px] text-nowrap whitespace-pre">{description}</p>
        </div>
      </div>
    </button>
  );
}
