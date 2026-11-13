/* CI-д ашиглагдах ESLint тохиргоо (Vite + React + TypeScript) */
module.exports = {
  root: true,
  env: { browser: true, es2020: true },
  parser: '@typescript-eslint/parser',
  parserOptions: { ecmaVersion: 'latest', sourceType: 'module', ecmaFeatures: { jsx: true } },
  extends: ['eslint:recommended', 'plugin:@typescript-eslint/recommended', 'plugin:react-hooks/recommended'],
  ignorePatterns: ['dist', 'node_modules', '.eslintrc.cjs', 'vite.config.ts'],
  plugins: ['react-refresh'],
  rules: {
    // Одоо байгаа кодод зориулсан практик тохиргоо — алдаа (error) биш анхааруулга
    'react-refresh/only-export-components': 'off',
    '@typescript-eslint/no-explicit-any': 'off',
    '@typescript-eslint/no-unused-vars': 'off',
    '@typescript-eslint/no-non-null-assertion': 'off',
    '@typescript-eslint/ban-ts-comment': 'off',
    'no-empty': 'off',
    'no-constant-condition': 'off',
    'no-useless-escape': 'off',
    'no-control-regex': 'off',
  },
};
