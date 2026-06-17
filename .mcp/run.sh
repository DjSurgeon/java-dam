#!/bin/bash
# Script de inicio para el servidor MCP de HamBooking
# Asegura que se ejecuta en el directorio del MCP

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
node "$SCRIPT_DIR/index.js"
