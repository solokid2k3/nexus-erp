import type { Metadata } from "next";
import "./globals.css";
import { AntdRegistry } from "@ant-design/nextjs-registry";
import { ConfigProvider } from "antd";
import { ClientProviders } from "@/components/ClientProviders";

export const metadata: Metadata = {
  title: "Nexus ERP",
  description: "Enterprise Resource Planning — HR, Inventory, Orders, Finance",
};

const theme = {
  token: {
    colorPrimary: "#111111",
    borderRadius: 8,
    fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif",
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>
        <AntdRegistry>
          <ConfigProvider theme={theme}>
            <ClientProviders>{children}</ClientProviders>
          </ConfigProvider>
        </AntdRegistry>
      </body>
    </html>
  );
}
