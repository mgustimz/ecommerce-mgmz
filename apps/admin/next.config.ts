import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  transpilePackages: ["@mgmz/api-client", "@mgmz/shared"]
};

export default nextConfig;
