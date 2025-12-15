import { Redirect, Route, Switch } from "wouter";

import { LoginScreen } from "@/screens/LoginScreen";
import { SignupScreen } from "@/screens/SignupScreen";
import AdminScreen from "@/screens/Admin/AdminScreen";
import IngredientsScreen from "@/screens/Admin/IngredientsScreen";
import ProductsScreen from "@/screens/Admin/ProductsScreen";
import PromotionsScreen from "@/screens/Admin/PromotionsScreen";
import { StaffDashboardScreen } from "@/screens/Staff/StaffDashboardScreen";
import { MenuPage } from "@/screens/Student/MenuPage";
import { OrderDetailPage } from "@/screens/Student/OrderDetailPage";
import { useToken } from "@/services/TokenContext";
import { RecoverPasswordScreen } from "@/screens/RecoverPasswordScreen";
import { ResetPasswordScreen } from "@/screens/ResetPasswordScreen";
import { EmailVerificationScreen } from "@/screens/EmailVerificationScreen"
import { ProfileScreen } from "@/screens/ProfileScreen";
import { RegistrationSuccessScreen } from "@/screens/RegistrationSuccessScreen";
import { RecoverSuccessScreen } from "@/screens/RecoverSuccessScreen";
import { PasswordResetSuccessScreen } from "@/screens/ResetPasswordSuccessScreen";
import { EditProfileScreen } from "./screens/EditProfileScreen";
import StaffManagementScreen from "@/screens/Admin/StaffManagementScreen";
import CombosScreen from "@/screens/Admin/CombosScreen";
import { StudentOrdersPage } from "@/screens/StudentOrdersPage";
import { StudentDashboardPage } from "@/screens/StudentDashboardPage";
import { StudentTrackingPage } from "@/screens/StudentTrackingPage";
import { StaffOrderManagementPage } from "@/screens/Staff/StaffOrderManagementPage";
import { useOrderSse } from "@/hooks/useOrderSse";
import { useCurrentUserEmail } from "@/hooks/useCurrentUserEmail";

export const Navigation = () => {
  const [tokenState] = useToken();
  const email = useCurrentUserEmail();

  const role =
    tokenState.state === "LOGGED_IN" ? tokenState.tokens.role : null;

  const isStudent = role === "STUDENT";

  useOrderSse(isStudent ? email : null);

  switch (tokenState.state) {
    case "LOGGED_IN":
    case "REFRESHING": {
      const tokens =
        tokenState.state === "LOGGED_IN" ? tokenState.tokens : null;

      const role = tokens ? (tokens as { role?: string }).role ?? null : null;

      return (
        <Switch>
          {/* RUTAS ESPECÍFICAS PRIMERO - con mayor especificidad */}
          
          {/* ADMIN routes */}
          <Route path="/admin/staff">
            {role === "ADMIN" ? <StaffManagementScreen /> : <Redirect href="/" />}
          </Route>
          <Route path="/admin/ingredients">
            {role === "ADMIN" ? <IngredientsScreen /> : <Redirect href="/" />}
          </Route>
          <Route path="/admin/products">
            {role === "ADMIN" ? <ProductsScreen /> : <Redirect href="/" />}
          </Route>
          <Route path="/admin/combos">
            {role === "ADMIN" ? <CombosScreen /> : <Redirect href="/" />}
          </Route>
          <Route path="/admin/promotions">
            {role === "ADMIN" ? <PromotionsScreen /> : <Redirect href="/" />}
          </Route>
          <Route path="/admin/:sub">{() => (role === "admin" ? <AdminScreen /> : <Redirect href="/" />)}</Route>
          <Route path="/admin">
            {role === "ADMIN" ? <AdminScreen /> : <Redirect href="/" />}
          </Route>

          {/* STAFF routes */}
          <Route path="/staff/orders">
            {role === "STAFF" ? <StaffOrderManagementPage /> : <Redirect href="/" />}
          </Route>
          <Route path="/staff">
            {role === "STAFF" ? <StaffDashboardScreen /> : <Redirect href="/" />}
          </Route>

          {/* STUDENT routes */}
          <Route path="/orders/:orderId">
            {role === "STUDENT" || !role ? <OrderDetailPage /> : <Redirect href="/" />}
          </Route>
          <Route path="/orders">
            {role === "STUDENT" || !role ? <StudentOrdersPage /> : <Redirect href="/" />}
          </Route>

          {/* COMMON routes */}
          <Route path="/profile/edit">
            <EditProfileScreen />
          </Route>
          <Route path="/profile">
            <ProfileScreen />
          </Route>
          <Route path="/menu">
            <MenuPage />
          </Route>
          <Route path="/dashboard">
            <StudentDashboardPage />
          </Route>
          <Route path="/tracking">
            <StudentTrackingPage />
          </Route>

          {/* ROOT path - fallback */}
          <Route path="/">
            {role === "ADMIN" ? <Redirect href="/admin" /> : role === "STAFF" ? <Redirect href="/staff" /> : <Redirect href="/menu" />}
          </Route>
          <Route>
            <Redirect href="/" />
          </Route>
        </Switch>
      );
    }
    case "LOGGED_OUT":
      return (
        <Switch>
          <Route path="/login">
            <LoginScreen />
          </Route>
          <Route path="/signup">
            <SignupScreen />
          </Route>
          <Route path="/registration-success">
            <RegistrationSuccessScreen />
          </Route>
          <Route path="/reset-request">
            <RecoverPasswordScreen />
          </Route>
          <Route path="/reset-request-sent">
            <RecoverSuccessScreen />
          </Route>
          <Route path="/verify-email">
            <EmailVerificationScreen />
          </Route>
          <Route path="/reset">
            <ResetPasswordScreen />
          </Route>
          <Route path="/reset-success">
            <PasswordResetSuccessScreen />
          </Route>
          <Route>
            <Redirect href="/login" />
          </Route>
        </Switch>
      );
  default:
      return tokenState satisfies never;
  }
};