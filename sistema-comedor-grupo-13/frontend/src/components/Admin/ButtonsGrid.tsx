import type { ReactNode } from 'react';
import AdminButton from './AdminButton';

interface ButtonSpec {
  icon: ReactNode;
  title: string;
  description: string;
  color?: string;
  onClick?: () => void;
}

interface ButtonsGridProps {
  buttons: ButtonSpec[];
}

export default function ButtonsGrid({ buttons }: ButtonsGridProps) {
  return (
    <div className="content-stretch flex flex-col gap-[10px] w-full">
      {buttons.map((b, idx) => (
        <AdminButton key={idx} icon={b.icon} title={b.title} description={b.description} color={b.color} onClick={b.onClick} />
      ))}
    </div>
  );
}
