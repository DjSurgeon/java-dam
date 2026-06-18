import { spawn, execSync } from 'child_process';
import fs from 'fs';
import path from 'path';
import readline from 'readline';
import https from 'https';

const PROJECT_ROOT = '/home/sergio/vscode/java-dam';
const BACKEND_DIR = path.join(PROJECT_ROOT, 'backend/backend');
const FRONTEND_DIR = path.join(PROJECT_ROOT, 'frontend/frontend');
const WEB_DIR = path.join(PROJECT_ROOT, 'frontend-web');
const DATABASE_DIR = path.join(PROJECT_ROOT, 'database');
const DOCS_DIR = path.join(PROJECT_ROOT, 'docs');
const MEMORY_FILE = path.join(PROJECT_ROOT, '.mcp/memory.json');

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
  terminal: false
});

rl.on('line', (line) => {
  if (!line.trim()) return;
  try {
    const request = JSON.parse(line);
    handleRequest(request);
  } catch (err) {
    sendError(null, -32700, "Parse error: " + err.message);
  }
});

function handleRequest(req) {
  const { jsonrpc, id, method, params } = req;
  
  if (jsonrpc !== '2.0') {
    return sendError(id, -32600, "Invalid Request: jsonrpc must be '2.0'");
  }

  console.error(`[MCP Server] Received method: ${method}`);

  switch (method) {
    case 'initialize':
      return sendResponse(id, {
        protocolVersion: '2024-11-05',
        capabilities: {
          tools: {}
        },
        serverInfo: {
          name: 'hambooking-mcp-server',
          version: '1.2.0'
        }
      });

    case 'notifications/initialized':
      return;

    case 'tools/list':
      return sendResponse(id, {
        tools: getToolsList()
      });

    case 'tools/call':
      if (!params || !params.name) {
        return sendError(id, -32602, "Invalid params: missing tool name");
      }
      return executeTool(id, params.name, params.arguments || {});

    default:
      return sendError(id, -32601, `Method not found: ${method}`);
  }
}

function sendResponse(id, result) {
  const response = {
    jsonrpc: '2.0',
    id,
    result
  };
  process.stdout.write(JSON.stringify(response) + '\n');
}

function sendError(id, code, message) {
  const response = {
    jsonrpc: '2.0',
    id,
    error: {
      code,
      message
    }
  };
  process.stdout.write(JSON.stringify(response) + '\n');
}

function getToolsList() {
  return [
    {
      name: "read_project_docs",
      description: "Lista y lee los documentos de diseño y guías técnicas en la carpeta /docs (diagramas de casos de uso, ER, defensa, etc.)",
      inputSchema: {
        type: "object",
        properties: {
          file: {
            type: "string",
            description: "Ruta relativa o nombre del archivo de documentación (ej: 'diagramas/er-diagram.md'). Si se omite, listará todos los documentos."
          }
        }
      }
    },
    {
      name: "analyze_java_class",
      description: "Busca y lee el contenido completo de una clase Java en el backend o frontend para entender su estructura, lógica y dependencias.",
      inputSchema: {
        type: "object",
        properties: {
          className: {
            type: "string",
            description: "Nombre de la clase Java (ej: 'ReservationService')"
          }
        },
        required: ["className"]
      }
    },
    {
      name: "run_maven_tests",
      description: "Ejecuta los tests unitarios en la carpeta backend usando './mvnw test' para verificar la estabilidad de la lógica de negocio.",
      inputSchema: {
        type: "object",
        properties: {}
      }
    },
    {
      name: "run_web_tests",
      description: "Ejecuta los tests unitarios y de componentes en el frontend web utilizando Vitest en modo ejecución única sin watch.",
      inputSchema: {
        type: "object",
        properties: {}
      }
    },
    {
      name: "analyze_database",
      description: "Permite analizar el modelo de datos de la aplicación leyendo el DDL (schema.sql) o ejecutando consultas SELECT de solo lectura contra MySQL local.",
      inputSchema: {
        type: "object",
        properties: {
          action: {
            type: "string",
            enum: ["read_schema", "query"],
            description: "Elige 'read_schema' para ver el esquema estático SQL o 'query' para ejecutar consultas SELECT."
          },
          sqlQuery: {
            type: "string",
            description: "Consulta SELECT a ejecutar (solo si action es 'query'). Debe ser una consulta de solo lectura."
          }
        },
        required: ["action"]
      }
    },
    {
      name: "propose_java_tests",
      description: "Genera un esqueleto de pruebas unitarias usando JUnit 5 y Mockito para una clase de negocio Java dada, con el objetivo de elevar la cobertura.",
      inputSchema: {
        type: "object",
        properties: {
          className: {
            type: "string",
            description: "Nombre de la clase Java (ej: 'ReservationService')"
          }
        },
        required: ["className"]
      }
    },
    {
      name: "propose_web_tests",
      description: "Genera un esqueleto de pruebas unitarias/componentes usando Vitest y React Testing Library para un componente o utilidad TypeScript dada.",
      inputSchema: {
        type: "object",
        properties: {
          className: {
            type: "string",
            description: "Nombre del componente o módulo TypeScript (ej: 'Logo')"
          }
        },
        required: ["className"]
      }
    },
    {
      name: "check_clean_code",
      description: "Verifica si una clase Java o componente TypeScript/React cumple con las reglas y mandatos del proyecto (JPA LAZY, Javadoc, comentarios inline, no 'any', arquitectura FSD, etc.).",
      inputSchema: {
        type: "object",
        properties: {
          className: {
            type: "string",
            description: "Nombre del archivo, componente o clase a analizar (ej: 'ReservationService' o 'Logo')"
          }
        },
        required: ["className"]
      }
    },
    {
      name: "fetch_official_docs",
      description: "Descarga o busca información en tiempo real de las fuentes de documentación oficial (Docker, Spring Boot, Maven o Java 21) vía HTTPS. Cae a una copia de respaldo local si está offline.",
      inputSchema: {
        type: "object",
        properties: {
          topic: {
            type: "string",
            enum: ["docker", "spring-boot", "maven", "java-21"],
            description: "Tema oficial a consultar."
          },
          query: {
            type: "string",
            description: "Texto opcional para filtrar contenidos específicos de la documentación (ej: 'multi-stage', 'cache', 'lazy')."
          }
        },
        required: ["topic"]
      }
    },
    {
      name: "remember_concept",
      description: "Guarda un nuevo aprendizaje, regla de diseño o buena práctica en el archivo local de memoria persistente (.mcp/memory.json).",
      inputSchema: {
        type: "object",
        properties: {
          type: {
            type: "string",
            enum: ["docker_rules", "project_lessons"],
            description: "Categoría de la memoria."
          },
          title: {
            type: "string",
            description: "Título del aprendizaje o regla (ej: 'Optimizando Docker Cache')."
          },
          description: {
            type: "string",
            description: "Breve explicación."
          },
          content: {
            type: "string",
            description: "Snippet de código, configuración o regla de texto completa."
          },
          tag: {
            type: "string",
            description: "Etiqueta identificativa (ej: 'docker', 'jpa', 'javafx')."
          }
        },
        required: ["type", "title", "description", "content"]
      }
    },
    {
      name: "recall_concepts",
      description: "Busca y devuelve los conceptos almacenados en la memoria persistente local.",
      inputSchema: {
        type: "object",
        properties: {
          type: {
            type: "string",
            enum: ["docker_rules", "project_lessons", "all"],
            description: "Filtrar por categoría (por defecto 'all')."
          },
          tag: {
            type: "string",
            description: "Filtrar por etiqueta (ej: 'spring-boot', 'mysql', 'javafx')."
          },
          query: {
            type: "string",
            description: "Búsqueda por texto en los títulos o contenido."
          }
        }
      }
    },
    {
      name: "propose_docker_configs",
      description: "Genera plantillas recomendadas de Dockerfile (Backend, JavaFX y React) y docker-compose.yml adaptadas al proyecto a partir de la memoria acumulada.",
      inputSchema: {
        type: "object",
        properties: {}
      }
    }
  ];
}

async function executeTool(id, name, args) {
  try {
    let responseText = '';
    let isError = false;

    switch (name) {
      case 'read_project_docs':
        responseText = handleReadDocs(args.file);
        break;

      case 'analyze_java_class':
        responseText = handleAnalyzeJavaClass(args.className);
        break;

      case 'run_maven_tests':
        responseText = await handleRunMavenTests();
        break;

      case 'run_web_tests':
        responseText = await handleRunWebTests();
        break;

      case 'analyze_database':
        responseText = handleAnalyzeDatabase(args.action, args.sqlQuery);
        break;

      case 'propose_java_tests':
        responseText = handleProposeJavaTests(args.className);
        break;

      case 'propose_web_tests':
        responseText = handleProposeWebTests(args.className);
        break;

      case 'check_clean_code':
        responseText = handleCheckCleanCode(args.className);
        break;

      case 'fetch_official_docs':
        responseText = await handleFetchOfficialDocs(args.topic, args.query);
        break;

      case 'remember_concept':
        responseText = handleRememberConcept(args.type, args.title, args.description, args.content, args.tag);
        break;

      case 'recall_concepts':
        responseText = handleRecallConcepts(args.type || 'all', args.tag, args.query);
        break;

      case 'propose_docker_configs':
        responseText = handleProposeDockerConfigs();
        break;

      default:
        return sendError(id, -32601, `Tool not found: ${name}`);
    }

    sendResponse(id, {
      content: [
        {
          type: "text",
          text: responseText
        }
      ],
      isError
    });
  } catch (err) {
    sendResponse(id, {
      content: [
        {
          type: "text",
          text: `Error ejecutando herramienta ${name}: ${err.message}`
        }
      ],
      isError: true
    });
  }
}

// 1. read_project_docs
function handleReadDocs(file) {
  if (file) {
    const filePath = path.resolve(DOCS_DIR, file);
    if (!filePath.startsWith(DOCS_DIR)) {
      throw new Error("Acceso denegado: Ruta fuera de /docs");
    }
    if (!fs.existsSync(filePath)) {
      return `El archivo de documentación ${file} no existe.`;
    }
    return fs.readFileSync(filePath, 'utf-8');
  } else {
    const listFiles = (dir) => {
      let results = [];
      const list = fs.readdirSync(dir);
      list.forEach((file) => {
        const fullPath = path.join(dir, file);
        const stat = fs.statSync(fullPath);
        if (stat && stat.isDirectory()) {
          results = results.concat(listFiles(fullPath));
        } else if (file.endsWith('.md')) {
          results.push(path.relative(DOCS_DIR, fullPath));
        }
      });
      return results;
    };
    const docs = listFiles(DOCS_DIR);
    return `Documentos de diseño disponibles en /docs:\n\n` + docs.map(d => `- ${d}`).join('\n');
  }
}

// Helper to find a Java file recursively
function findJavaFile(dir, className) {
  if (!fs.existsSync(dir)) return null;
  const files = fs.readdirSync(dir);
  for (const file of files) {
    const fullPath = path.join(dir, file);
    const stat = fs.statSync(fullPath);
    if (stat.isDirectory()) {
      const found = findJavaFile(fullPath, className);
      if (found) return found;
    } else if (file === `${className}.java`) {
      return fullPath;
    }
  }
  return null;
}

// Helper to find a TypeScript/React file recursively
function findWebFile(dir, className) {
  if (!fs.existsSync(dir)) return null;
  const files = fs.readdirSync(dir);
  for (const file of files) {
    const fullPath = path.join(dir, file);
    const stat = fs.statSync(fullPath);
    if (stat.isDirectory()) {
      const found = findWebFile(fullPath, className);
      if (found) return found;
    } else if (file === `${className}.tsx` || file === `${className}.ts`) {
      return fullPath;
    }
  }
  return null;
}

// 2. analyze_java_class
function handleAnalyzeJavaClass(className) {
  const backendFile = findJavaFile(BACKEND_DIR, className);
  const frontendFile = findJavaFile(FRONTEND_DIR, className);
  const foundFile = backendFile || frontendFile;

  if (!foundFile) {
    return `No se encontró la clase Java '${className}' en el backend ni en el frontend.`;
  }

  const content = fs.readFileSync(foundFile, 'utf-8');
  const relPath = path.relative(PROJECT_ROOT, foundFile);
  return `=== Clase: ${className} (${relPath}) ===\n\n${content}`;
}

// 3. run_maven_tests
function handleRunMavenTests() {
  return new Promise((resolve, reject) => {
    console.error("[MCP Server] Iniciando ejecución de tests con ./mvnw test...");
    const child = spawn('./mvnw', ['test'], { cwd: BACKEND_DIR });

    let stdout = '';
    let stderr = '';

    child.stdout.on('data', (data) => {
      const chunk = data.toString();
      stdout += chunk;
      process.stderr.write(chunk);
    });

    child.stderr.on('data', (data) => {
      const chunk = data.toString();
      stderr += chunk;
      process.stderr.write(chunk);
    });

    child.on('close', (code) => {
      const statusText = code === 0 ? "ÉXITO" : "FALLÓ";
      const lines = stdout.split('\n');
      const summaryLines = lines.filter(line => 
        line.includes('Tests run:') || line.includes('BUILD SUCCESS') || line.includes('BUILD FAILURE')
      );
      
      resolve(`=== Resultado de la suite de pruebas Java: ${statusText} (Código de salida: ${code}) ===\n\n` +
        `Resumen de ejecución:\n${summaryLines.join('\n')}\n\n` +
        `Si hubo fallos, revisa la consola para ver los detalles del error.`);
    });

    child.on('error', (err) => {
      reject(new Error(`No se pudo arrancar ./mvnw: ${err.message}`));
    });
  });
}

// 3.5 run_web_tests
function handleRunWebTests() {
  return new Promise((resolve, reject) => {
    console.error("[MCP Server] Iniciando ejecución de tests web con npm run test...");
    
    // Ejecutamos Vitest en modo "run" (no-watch) para ejecución única en CI/IDE
    const child = spawn('npx', ['vitest', 'run'], { cwd: WEB_DIR });

    let stdout = '';
    let stderr = '';

    child.stdout.on('data', (data) => {
      const chunk = data.toString();
      stdout += chunk;
      process.stderr.write(chunk);
    });

    child.stderr.on('data', (data) => {
      const chunk = data.toString();
      stderr += chunk;
      process.stderr.write(chunk);
    });

    child.on('close', (code) => {
      const statusText = code === 0 ? "ÉXITO" : "FALLÓ";
      resolve(`=== Resultado de la suite de pruebas Web: ${statusText} (Código de salida: ${code}) ===\n\n` +
        `Salida estándar:\n${stdout}\n\n` +
        `Salida de error (si aplica):\n${stderr}`);
    });

    child.on('error', (err) => {
      reject(new Error(`No se pudo arrancar vitest: ${err.message}`));
    });
  });
}

// 4. analyze_database
function handleAnalyzeDatabase(action, sqlQuery) {
  if (action === 'read_schema') {
    const schemaPath = path.join(DATABASE_DIR, 'schema.sql');
    if (!fs.existsSync(schemaPath)) {
      return `No se encontró el archivo de esquema en: ${schemaPath}`;
    }
    const content = fs.readFileSync(schemaPath, 'utf-8');
    return `=== Esquema de Base de Datos (DDL) ===\n\n${content}`;
  }

  if (action === 'query') {
    if (!sqlQuery) {
      throw new Error("Se requiere 'sqlQuery' cuando la acción es 'query'");
    }

    if (!/^\s*(select|show|describe|explain)\b/i.test(sqlQuery)) {
      return "Acceso denegado: Solo se permiten consultas de lectura (SELECT, SHOW, DESCRIBE, EXPLAIN) para evitar modificar datos.";
    }

    try {
      console.error(`[MCP Server] Ejecutando query SQL: ${sqlQuery}`);
      const output = execSync(`mysql -u root -psergio1234 -e "${sqlQuery}" hambooking`, { encoding: 'utf-8' });
      return `=== Resultado de la consulta ===\n\n${output || '(Sin resultados)'}`;
    } catch (err) {
      return `Error al ejecutar la consulta SQL en la base de datos local: ${err.stderr || err.message}`;
    }
  }

  throw new Error(`Acción desconocida: ${action}`);
}

// 5. propose_java_tests
function handleProposeJavaTests(className) {
  const backendFile = findJavaFile(BACKEND_DIR, className);
  if (!backendFile) {
    return `No se encontró la clase '${className}' en el backend para proponer pruebas.`;
  }

  const content = fs.readFileSync(backendFile, 'utf-8');
  const packageMatch = content.match(/package\s+([^;]+);/);
  const packageName = packageMatch ? packageMatch[1] : 'com.hambooking';

  const methodRegex = /public\s+[\w<>[\]]+\s+(\w+)\s*\(([^)]*)\)/g;
  const methods = [];
  let match;
  while ((match = methodRegex.exec(content)) !== null) {
    const methodName = match[1];
    const args = match[2];
    if (methodName !== className && !['toString', 'equals', 'hashCode'].includes(methodName)) {
      methods.push({ methodName, args });
    }
  }

  const dependencies = [];
  const lines = content.split('\n');
  lines.forEach(line => {
    const depMatch = line.match(/private\s+(?:final\s+)?([A-Z]\w+)\s+(\w+);/);
    if (depMatch) {
      const type = depMatch[1];
      const name = depMatch[2];
      if (!['String', 'Long', 'Integer', 'Boolean', 'Double', 'List', 'Set', 'Map'].includes(type)) {
        dependencies.push({ type, name });
      }
    }
  });

  const varName = className.charAt(0).toLowerCase() + className.slice(1);

  let testClass = `package ${packageName};\n\n`;
  testClass += `import org.junit.jupiter.api.Test;\n`;
  testClass += `import org.junit.jupiter.api.extension.ExtendWith;\n`;
  testClass += `import org.mockito.InjectMocks;\n`;
  testClass += `import org.mockito.Mock;\n`;
  testClass += `import org.mockito.junit.jupiter.MockitoExtension;\n`;
  testClass += `import static org.junit.jupiter.api.Assertions.*;\n`;
  testClass += `import static org.mockito.Mockito.*;\n\n`;
  testClass += `/**\n`;
  testClass += ` * Pruebas unitarias para {@link ${className}}.\n`;
  testClass += ` * Generado por HamBooking MCP Server.\n`;
  testClass += ` */\n`;
  testClass += `@ExtendWith(MockitoExtension.class)\n`;
  testClass += `class ${className}Test {\n\n`;

  if (dependencies.length > 0) {
    dependencies.forEach(dep => {
      testClass += `    @Mock\n`;
      testClass += `    private ${dep.type} ${dep.name};\n\n`;
    });
  }

  testClass += `    @InjectMocks\n`;
  testClass += `    private ${className} ${varName};\n\n`;

  if (methods.length === 0) {
    testClass += `    // No se detectaron métodos públicos adicionales para testear.\n`;
  } else {
    methods.forEach(m => {
      testClass += `    @Test\n`;
      testClass += `    void ${m.methodName}_DeberiaComportarseCorrectamente_CuandoCondicion() {\n`;
      testClass += `        // Arrange (Configuración de mocks e inputs)\n`;
      testClass += `        // TODO: Configurar mocks de dependencias\n\n`;
      testClass += `        // Act (Llamada al método bajo prueba)\n`;
      testClass += `        // ${varName}.${m.methodName}(...);\n\n`;
      testClass += `        // Assert (Verificación de resultados y llamadas)\n`;
      testClass += `        // fail("Test no implementado aún");\n`;
      testClass += `    }\n\n`;
    });
  }

  testClass += `}\n`;

  return `=== Propuesta de Pruebas Unitarias para ${className} ===\n\n` +
    `Aquí tienes el esqueleto sugerido para tus tests usando JUnit 5 y Mockito. Copia y adapta este archivo en la carpeta 'src/test/java':\n\n` +
    `\`\`\`java\n${testClass}\`\`\``;
}

// 5.5 propose_web_tests
function handleProposeWebTests(className) {
  const webFile = findWebFile(path.join(WEB_DIR, 'src'), className);
  if (!webFile) {
    return `No se encontró el componente/archivo '${className}' en frontend-web/src para proponer pruebas.`;
  }

  const content = fs.readFileSync(webFile, 'utf-8');
  const isReact = content.includes('React') || content.includes('import') || content.includes('export');

  let testCode = '';
  if (isReact) {
    testCode = `import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import { ${className} } from './${className}'; // Ajustar ruta según corresponda

describe('${className} Component', () => {
  it('debería renderizarse correctamente', () => {
    render(<${className} />);
    // TODO: Agregar aserciones correspondientes
  });

  it('debería responder a las interacciones del usuario', async () => {
    const user = userEvent.setup();
    render(<${className} />);
    // TODO: Simular interacciones
  });
});
`;
  } else {
    testCode = `import { describe, it, expect } from 'vitest';
import { ${className} } from './${className}';

describe('${className}', () => {
  it('debería ejecutar su lógica correctamente', () => {
    // TODO: Implementar prueba unitaria pura
  });
});
`;
  }

  return `=== Propuesta de Pruebas Unitarias Web para ${className} ===\n\n` +
    `Aquí tienes el esqueleto sugerido para tus tests usando Vitest y React Testing Library. Créalo como '${className}.test.tsx' en el mismo directorio:\n\n` +
    `\`\`\`tsx\n${testCode}\`\`\``;
}

// 6. check_clean_code
function handleCheckCleanCode(className) {
  const backendFile = findJavaFile(BACKEND_DIR, className);
  const frontendFile = findJavaFile(FRONTEND_DIR, className);
  const webFile = findWebFile(path.join(WEB_DIR, 'src'), className);
  const foundFile = backendFile || frontendFile || webFile;

  if (!foundFile) {
    return `No se encontró la clase o componente '${className}' en el backend, frontend JavaFX ni en el frontend Web.`;
  }

  const content = fs.readFileSync(foundFile, 'utf-8');
  const lines = content.split('\n');
  const issues = [];
  const isWeb = foundFile.endsWith('.ts') || foundFile.endsWith('.tsx');

  if (isWeb) {
    // TypeScript & React FSD Audit
    let inlineCommentCount = 0;
    lines.forEach((line, idx) => {
      // Inline comments check
      if (line.includes('//') && !line.match(/https?:\/\//)) {
        inlineCommentCount++;
        issues.push(`Línea ${idx + 1}: Comentario inline detectado: "${line.trim()}" (Mandato: No usar // para documentación primaria).`);
      }
      
      // Strict TypeScript: no 'any'
      if (/\bany\b/.test(line) && !line.includes('//') && !line.includes('*')) {
        issues.push(`Línea ${idx + 1}: Uso del tipo 'any' detectado: "${line.trim()}" (Mandato TS: estricto, no usar 'any').`);
      }

      // FSD Architecture Path Aliases Check
      if (/import\s+.*\s+from\s+['"]\.\.?\//.test(line)) {
        if (line.includes("'../") || line.includes('"../')) {
          issues.push(`Línea ${idx + 1}: Importación relativa hacia niveles superiores detectada: "${line.trim()}" (Mandato: Usar el alias absoluto '@/' para importar fuera del slice).`);
        }
      }

      // FSD Layers Strictly Downward Import Rules
      // Layers: app > pages > widgets > features > entities > shared
      const layers = ['app', 'pages', 'widgets', 'features', 'entities', 'shared'];
      const fileLayer = layers.find(l => foundFile.includes(`/src/${l}/`));
      if (fileLayer) {
        const fileLayerIndex = layers.indexOf(fileLayer);
        layers.slice(0, fileLayerIndex).forEach(higherLayer => {
          if (line.includes(`@/${higherLayer}/`) || line.includes(`from '@/${higherLayer}`)) {
            issues.push(`Línea ${idx + 1}: Violación FSD detectada. El archivo en '${fileLayer}' no debe importar de una capa superior '${higherLayer}': "${line.trim()}".`);
          }
        });
      }
    });

    if (issues.length === 0) {
      return `=== Auditoría Clean Code Web: ${className} ===\n\n` +
        `¡Excelente! No se encontraron desviaciones respecto a los mandatos FSD/TS de GEMINI.md.\n` +
        `- Sin comentarios inline (//) sospechosos.\n` +
        `- Sin tipos 'any'.\n` +
        `- Importaciones relativas limpias y alineadas con FSD.\n` +
        `- Respeto estricto del orden descendente de capas FSD.`;
    }

    return `=== Auditoría Clean Code Web: ${className} ===\n\n` +
      `Se encontraron ${issues.length} advertencias según los mandatos FSD/TS:\n\n` +
      issues.map(issue => `- ${issue}`).join('\n') + `\n\n` +
      `Recomendación: Refactoriza el componente/archivo para cumplir con la arquitectura FSD y estándares TS.`;

  } else {
    // Java audit
    let inlineCommentCount = 0;
    lines.forEach((line, idx) => {
      if (line.includes('//') && !line.match(/https?:\/\//)) {
        inlineCommentCount++;
        issues.push(`Línea ${idx + 1}: Comentario inline detectado: "${line.trim()}" (Mandato: No usar // para documentación primaria).`);
      }
    });

    const classDeclIdx = lines.findIndex(line => line.match(/(class|interface|enum)\s+\w+/));
    if (classDeclIdx !== -1) {
      let hasJavadoc = false;
      for (let i = Math.max(0, classDeclIdx - 5); i < classDeclIdx; i++) {
        if (lines[i].includes('/**') || lines[i].includes('*')) {
          hasJavadoc = true;
          break;
        }
      }
      if (!hasJavadoc) {
        issues.push(`Clase principal '${className}': No tiene Javadoc decorativo (/** ... */) en la declaración.`);
      }
    }

    lines.forEach((line, idx) => {
      if (line.includes('@OneToMany') || line.includes('@ManyToMany') || line.includes('@ManyToOne') || line.includes('@OneToOne')) {
        if (!line.includes('FetchType.LAZY') && !line.includes('fetch = FetchType.LAZY')) {
          issues.push(`Línea ${idx + 1}: Asociación JPA detectada sin FetchType.LAZY explícito: "${line.trim()}" (Mandato: usar LAZY por defecto).`);
        }
      }
    });

    if (issues.length === 0) {
      return `=== Auditoría Clean Code Java: ${className} ===\n\n` +
        `¡Excelente! No se encontraron desviaciones respecto a los mandatos de GEMINI.md.\n` +
        `- Cumple con Javadoc obligatorio.\n` +
        `- Sin comentarios inline (//) sospechosos.\n` +
        `- Todas las asociaciones JPA usan FetchType.LAZY.`;
    }

    return `=== Auditoría Clean Code Java: ${className} ===\n\n` +
      `Se encontraron ${issues.length} advertencias según los mandatos de GEMINI.md:\n\n` +
      issues.map(issue => `- ${issue}`).join('\n') + `\n\n` +
      `Recomendación: Refactoriza la clase para corregir estos puntos antes de finalizar tu commit.`;
  }
}

// 7. fetch_official_docs
function handleFetchOfficialDocs(topic, query) {
  return new Promise((resolve) => {
    const docUrls = {
      'docker': 'https://raw.githubusercontent.com/docker/docker.github.io/master/develop/develop-images/dockerfile_best-practices.md',
      'spring-boot': 'https://raw.githubusercontent.com/spring-projects/spring-boot/main/README.md',
      'maven': 'https://raw.githubusercontent.com/apache/maven/master/README.md',
      'java-21': 'https://raw.githubusercontent.com/openjdk/jdk21/master/README.md'
    };
    
    const url = docUrls[topic];
    if (!url) {
      return resolve(`Tema desconocido para documentación: ${topic}`);
    }
    
    console.error(`[MCP Server] Intentando conectar a documentación oficial: ${url}`);
    
    const req = https.get(url, { timeout: 3000 }, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          let text = data;
          if (query) {
            const lines = text.split('\n');
            const matches = lines.filter(line => line.toLowerCase().includes(query.toLowerCase()));
            if (matches.length > 0) {
              text = `=== Coincidencias de búsqueda para '${query}' en documentación oficial ===\n\n` +
                matches.slice(0, 30).join('\n') + 
                (matches.length > 30 ? `\n... (y ${matches.length - 30} líneas más)` : '');
            } else {
              text = `No se encontraron coincidencias para '${query}' en la documentación en línea. Mostrando inicio:\n\n` +
                text.slice(0, 1000) + '...';
            }
          } else {
            text = text.slice(0, 3000) + '\n... (truncado para evitar sobrecarga)';
          }
          resolve(`=== [ONLINE] Documentación Oficial de ${topic.toUpperCase()} ===\n\n${text}`);
        } else {
          resolve(getOfflineFallback(topic, query, `Servidor retornó estado HTTP ${res.statusCode}`));
        }
      });
    });
    
    req.on('error', (err) => {
      resolve(getOfflineFallback(topic, query, `Error de red: ${err.message}`));
    });
    
    req.on('timeout', () => {
      req.destroy();
      resolve(getOfflineFallback(topic, query, "Timeout de conexión (límite de 3 segundos superado)"));
    });
  });
}

function getOfflineFallback(topic, query, reason) {
  console.error(`[MCP Server] Red no disponible (${reason}). Utilizando fallback offline...`);
  const memory = readMemory();
  const rules = memory.docker_rules.concat(memory.project_lessons)
    .filter(item => item.tag === topic || (item.tag && item.tag.includes(topic)));
  
  if (rules.length === 0) {
    return `=== [OFFLINE FALLBACK] Documentación de ${topic.toUpperCase()} ===\n\n` +
      `No se pudo contactar con la documentación oficial (${reason}) y no hay reglas locales guardadas para este tema.`;
  }
  
  let responseText = `=== [OFFLINE FALLBACK] Documentación de ${topic.toUpperCase()} (${reason}) ===\n\n` +
    `Mostrando reglas de respaldo locales almacenadas en el proyecto:\n\n`;
  
  rules.forEach(rule => {
    responseText += `--- [${rule.title}] ---\nEtiqueta: ${rule.tag}\nDescripción: ${rule.description}\nContenido:\n${rule.content}\n\n`;
  });
  
  return responseText;
}

// Helper memory functions
function readMemory() {
  if (!fs.existsSync(MEMORY_FILE)) {
    return { docker_rules: [], project_lessons: [] };
  }
  try {
    return JSON.parse(fs.readFileSync(MEMORY_FILE, 'utf-8'));
  } catch (err) {
    console.error(`[MCP Server] Error al leer memory.json: ${err.message}`);
    return { docker_rules: [], project_lessons: [] };
  }
}

function writeMemory(data) {
  try {
    fs.writeFileSync(MEMORY_FILE, JSON.stringify(data, null, 2), 'utf-8');
    return true;
  } catch (err) {
    console.error(`[MCP Server] Error al escribir memory.json: ${err.message}`);
    return false;
  }
}

// 8. remember_concept
function handleRememberConcept(type, title, description, content, tag) {
  const memory = readMemory();
  if (!memory[type]) {
    memory[type] = [];
  }
  
  const id = `${type === 'docker_rules' ? 'db' : 'pl'}-${Date.now()}`;
  const newItem = { id, tag: tag || '', title, description, content };
  memory[type].push(newItem);
  
  if (writeMemory(memory)) {
    return `Concepto guardado con éxito en la memoria persistente local con ID: ${id}.`;
  } else {
    throw new Error("No se pudo escribir en el archivo de memoria.");
  }
}

// 9. recall_concepts
function handleRecallConcepts(type, tag, query) {
  const memory = readMemory();
  let items = [];
  
  if (type === 'all') {
    items = memory.docker_rules.concat(memory.project_lessons);
  } else {
    items = memory[type] || [];
  }
  
  if (tag) {
    items = items.filter(item => item.tag && item.tag.toLowerCase() === tag.toLowerCase());
  }
  
  if (query) {
    const q = query.toLowerCase();
    items = items.filter(item => 
      (item.title && item.title.toLowerCase().includes(q)) ||
      (item.description && item.description.toLowerCase().includes(q)) ||
      (item.content && item.content.toLowerCase().includes(q))
    );
  }
  
  if (items.length === 0) {
    return "No se encontraron conceptos en la memoria que coincidan con los filtros.";
  }
  
  let responseText = `=== Conceptos en Memoria (${items.length} encontrados) ===\n\n`;
  items.forEach(item => {
    responseText += `ID: ${item.id}\nTítulo: ${item.title}\nEtiqueta: ${item.tag}\nDescripción: ${item.description}\nContenido:\n${item.content}\n\n========================================\n\n`;
  });
  
  return responseText;
}

// 10. propose_docker_configs
function handleProposeDockerConfigs() {
  const memory = readMemory();
  const dockerRules = memory.docker_rules || [];
  
  const rulesSummary = dockerRules.map(rule => `- **${rule.title}**: ${rule.description}`).join('\n');
  
  const backendRule = dockerRules.find(r => r.id === 'db-1');

  return `=== Propuesta de Configuración Docker para HamBooking ===\n\n` +
    `Basado en las reglas de Docker acumuladas en la memoria:\n${rulesSummary}\n\n` +
    `Aquí tienes los archivos propuestos para la dockerización completa del entorno:\n\n` +
    `### 1. [NUEVO] backend/backend/Dockerfile (Multi-stage)\n` +
    `\`\`\`dockerfile\n${backendRule ? backendRule.content : '# Dockerfile backend sin datos'}\n\`\`\`\n\n` +
    `### 2. [NUEVO] frontend-web/Dockerfile (React + Vite + TS Multi-stage)\n` +
    `\`\`\`dockerfile\n` +
    `FROM node:20-alpine AS base\n` +
    `WORKDIR /app\n` +
    `COPY package*.json ./\n` +
    `RUN npm ci\n\n` +
    `FROM base AS development\n` +
    `COPY . .\n` +
    `EXPOSE 3000\n` +
    `CMD [\"npm\", \"run\", \"dev\"]\n\n` +
    `FROM base AS builder\n` +
    `COPY . .\n` +
    `RUN npm run build\n\n` +
    `FROM nginx:stable-alpine AS production\n` +
    `COPY --from=builder /app/dist /usr/share/nginx/html\n` +
    `EXPOSE 80\n` +
    `CMD [\"nginx\", \"-g\", \"daemon off;\"]\n` +
    `\`\`\`\n\n` +
    `### 3. [MODIFICADO] docker-compose.yml (Raíz del proyecto)\n` +
    `\`\`\`yaml\n` +
    `version: '3.8'\n\n` +
    `services:\n` +
    `  db:\n` +
    `    image: mysql:8.0\n` +
    `    container_name: hambooking-db\n` +
    `    ports:\n` +
    `      - \"3306:3306\"\n` +
    `    volumes:\n` +
    `      - db_data:/var/lib/mysql\n` +
    `      - ./database/schema.sql:/docker-entrypoint-initdb.d/schema.sql:ro,z\n` +
    `    environment:\n` +
    `      MYSQL_DATABASE: hambooking\n` +
    `      MYSQL_ROOT_PASSWORD: sergio1234\n` +
    `    healthcheck:\n` +
    `      test: [\"CMD\", \"mysqladmin\", \"ping\", \"-h\", \"localhost\", \"-u\", \"root\", \"-psergio1234\"]\n` +
    `      interval: 10s\n` +
    `      timeout: 5s\n` +
    `      retries: 5\n` +
    `    deploy:\n` +
    `      resources:\n` +
    `        limits:\n` +
    `          cpus: '1.0'\n` +
    `          memory: 512M\n\n` +
    `  backend:\n` +
    `    build:\n` +
    `      context: ./backend/backend\n` +
    `    container_name: hambooking-backend\n` +
    `    ports:\n` +
    `      - \"8080:8080\"\n` +
    `    depends_on:\n` +
    `      db:\n` +
    `        condition: service_healthy\n` +
    `    environment:\n` +
    `      - SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/hambooking\n` +
    `      - SPRING_DATASOURCE_USERNAME=root\n` +
    `      - SPRING_DATASOURCE_PASSWORD=sergio1234\n` +
    `    deploy:\n` +
    `      resources:\n` +
    `        limits:\n` +
    `          cpus: '1.5'\n` +
    `          memory: 1024M\n\n` +
    `  frontend-web:\n` +
    `    build:\n` +
    `      context: ./frontend-web\n` +
    `      target: development\n` +
    `    container_name: hambooking-frontend-web\n` +
    `    ports:\n` +
    `      - \"3000:3000\"\n` +
    `    volumes:\n` +
    `      - ./frontend-web:/app:z\n` +
    `      - /app/node_modules\n` +
    `    depends_on:\n54` +
    `      backend:\n` +
    `        condition: service_started\n` +
    `    deploy:\n` +
    `      resources:\n` +
    `        limits:\n` +
    `          cpus: '1.5'\n` +
    `          memory: 1024M\n` +
    `\`\`\``;
}
