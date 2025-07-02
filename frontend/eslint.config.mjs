import globals from 'globals';
import stylistic from '@stylistic/eslint-plugin';
import tsParser from '@typescript-eslint/parser';
import tsPlugin from '@typescript-eslint/eslint-plugin';
import tsdoc from 'eslint-plugin-tsdoc';
import vue from 'eslint-plugin-vue';
import vueParser from 'vue-eslint-parser';
import { defineConfig } from 'eslint/config';

const plugins = {
  '@stylistic': stylistic,
  '@typescript-eslint': tsPlugin,
  tsdoc,
  vue
};

const parseConfig = {
  files: ['**/*.vue', '**/*.ts'],
  languageOptions: {
    parser: vueParser,
    parserOptions: {
      ecmaFeatures: { jsx: false },
      ecmaVersion: 'latest',
      extraFileExtensions: ['.vue'],
      parser: tsParser,
      sourceType: 'module'
    },
    globals: { ...globals.browser }
  }
};

const ignored = Object.fromEntries([
  'vue/singleline-html-element-content-newline',
  'vue/html-closing-bracket-spacing',
  'vue/no-multiple-template-root',
  'vue/order-in-components',
  'vue/attributes-order'
].map(rule => [rule, 'off']));

const rules = {
  ...tsPlugin.configs.recommended.rules,
  ...ignored,
  'tsdoc/syntax': 'warn',
  '@typescript-eslint/no-unused-vars': ['warn',  {
    'args': 'all',
    'argsIgnorePattern': '^_',
    'caughtErrors': 'all',
    'caughtErrorsIgnorePattern': '^_',
    'destructuredArrayIgnorePattern': '^_',
    'varsIgnorePattern': '^_',
    'ignoreRestSiblings': true
  }],
  'vue/html-quotes': ['error', 'double'],
  'vue/component-name-in-template-casing': ['error', 'kebab-case', {
    'registeredComponentsOnly': false,
    'ignores': []
  }],
  '@stylistic/semi': 'error',
  '@stylistic/type-annotation-spacing': ['error', {
    before: false,
    after: true,
    overrides: {
      arrow: { before: true, after: true }
    }
  }],
  '@stylistic/comma-dangle': ['error', 'never'],
  '@stylistic/eol-last': ['error', 'always'],
  '@stylistic/indent': ['error', 2],
  '@stylistic/no-multiple-empty-lines': ['error', { max: 1, maxEOF: 0 }],
  '@stylistic/no-trailing-spaces': 'error',
  'no-restricted-imports': ['error', {
    patterns: [{
      group: ['.*', '..*'],
      message: 'Please use absolute imports with "@"'
    }]
  }],
  'no-self-compare': 'error',
  'no-unmodified-loop-condition': 'warn',
  'no-unreachable-loop': 'error',
  'prefer-const': 'error',
  'quotes': ['error', 'single', { avoidEscape: true }],
  'key-spacing': ['error', {
    beforeColon: false,
    afterColon: true
  }]
};

export default defineConfig([
  ...vue.configs['flat/recommended'],
  { ignores: ['*.d.ts', '**/dist', 'vite.config.ts'] },
  {...parseConfig },
  { plugins, rules }
]);
