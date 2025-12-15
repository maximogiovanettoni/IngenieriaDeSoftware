import { CreateStaffForm } from "@/components/CreateStaffForm/CreateStaffForm";

export const AdminDashboardScreen = () => {
  return (
    <div style={{ padding: "1rem" }}>
      <h1>Panel de Administración</h1>
      <CreateStaffForm />
    </div>
  );
};
