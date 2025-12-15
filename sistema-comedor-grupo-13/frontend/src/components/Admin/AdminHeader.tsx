import { Users } from 'lucide-react';

export default function AdminHeader() {
  return (
    <div className="h-[102px] relative shrink-0 w-full">
      <div className="absolute box-border content-stretch flex items-center justify-center left-[176px] rounded-[3.35544e+07px] shadow-[0px_10px_15px_-3px_rgba(0,0,0,0.1),0px_4px_6px_-4px_rgba(0,0,0,0.1)] size-[56px] top-0">
        <div className="relative shrink-0 size-[28px]">
          <div className="absolute inset-0 bg-gradient-to-br from-[#155dfc] to-[#2b7fff] rounded-full flex items-center justify-center">
            <Users className="text-white" size={18} />
          </div>
        </div>
      </div>
      <div className="absolute h-[20px] left-0 top-[64px] w-[448px]">
        <p className="absolute font-['Arimo:Regular',_sans-serif] font-normal leading-[20px] left-[224.3px] text-[14px] text-center text-nowrap text-white top-0 translate-x-[-50%] whitespace-pre">Panel de Administración</p>
      </div>
      <div className="absolute h-[18px] left-0 top-[84px] w-[448px]">
        <p className="absolute font-['Arimo:Regular',_sans-serif] font-normal leading-[18px] left-[224.25px] text-[#99a1af] text-[12px] text-center text-nowrap top-0 translate-x-[-50%] whitespace-pre">Selecciona una sección para administrar</p>
      </div>
    </div>
  );
}
