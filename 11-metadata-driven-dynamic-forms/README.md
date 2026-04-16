# Backend MetaData-Driven Dynamic Form System - Complete Requirements & Design Criteria

**Role:**  
You are a senior backend architect and API designer with deep expertise in metadata-driven UIs, annotation-driven
systems, and enterprise form platforms using Java/Spring Boot.

## Objective

Design a complete, detailed set of **requirements and design criteria** for a powerful **annotation-driven** backend
metadata API that enables fully automatic, dynamic forms in modern frontends (especially Angular).

The backend should scan annotated Java classes (DTOs or dedicated View classes) using reflection and generate rich,
hierarchical **JSON metadata**. This metadata will allow the frontend to dynamically render fields, handle validation,
support complex structures, and manage submit/cancel actions without hardcoding forms.

---

## Core Concepts

- A **"View"** represents a complete form or screen definition.
- Views are defined primarily through **Java annotations** on classes and fields.
- The system must support:
    - Simple primitive fields
    - Nested objects (sub-forms)
    - Advanced collections: Lists, Sets, and Tables
    - Rich selection inputs (single & multiple)
    - Highly configurable input fields

---

## Annotation-Driven Approach (Mandatory)

The metadata generation **must** be primarily **annotation-driven**. Define clear requirements for:

- Custom annotations such as `@FormView`, `@FormField`, `@FormList`, `@FormOption`, etc.
- How annotations are applied to classes, fields, and collection elements
- Integration with Jakarta Validation annotations (`@NotNull`, `@NotBlank`, `@Size`, `@Email`, `@Pattern`, `@Min`,
  `@Max`, etc.) for automatic validation mapping
- Support for default values, masks, options, and conditional logic directly in annotations
- Reflection-based scanning to build the complete metadata JSON from annotated classes

---

## Comprehensive Field Properties

Every field must support the following properties, configurable mainly via annotations:

- **`key`** — Unique identifier (support hierarchical/dot notation for nested fields)
- **`label`** — Human-readable label with i18n support
- **`type`** — Field type (`text`, `email`, `password`, `number`, `decimal`, `integer`, `date`, `datetime`, `textarea`,
  `select`, `radio`, `checkbox`, `multiselect`, `autocomplete`, `object`, `list`, `set`, `table`, etc.)
- **`defaultValue`** — Initial value (primitives, objects, or arrays)
- **`required`** — Whether the field is mandatory
- **`minLength`**, **`maxLength`**, **`minValue`**, **`maxValue`**
- **`pattern`** — Regex validation pattern
- **`format`** — Formatting rules (currency, percentage, phone, date format, etc.)
- **`display`** — `"input"` (editable) or `"output"` (read-only)
- **`placeholder`**, **`tooltip`**, **`helpText`**
- **`order`** — Rendering order
- **`conditional`** — Rules for visibility, enable/disable, or required state based on other fields
- **`useMask`** + **`mask`** — Input mask configuration
- **`prefix`**, **`suffix`**, **`thousandSeparator`**, **`decimalSeparator`**
- Additional UX properties (`icon`, `size`, `variant`, `clearable`, `disabled`, etc.)

---

## Enhanced Requirements for Input Fields

For all input-type fields (`text`, `number`, `date`, `textarea`, etc.), define detailed support for:

- Editing behavior: `editable`, `display` (`input`/`output`), `disabled`, `readonly`
- Requirement handling: `required` / `optional`, custom required messages
- Input masking: `useMask`, `mask`, `maskOptions`
- Formatting: `format`, `prefix`, `suffix`, thousand/decimal separators
- Validation messages per rule
- Visual and interaction hints (`icon`, `size`, `variant`, `clearable`, `debounceTime`, `trim`, `spellcheck`, `maxRows`)

---

## Enhanced Requirements for Options / Selection Inputs

For selection fields (`select`, `radio`, `checkbox`, `multiselect`, `autocomplete`):

- **`multiple`**, **`searchable`**, **`clearable`**, **`showSelectAll`**, **`maxSelectable`**
- **`options`** — Rich option definition (`value`, `label`, `description`, `disabled`, `group`, `icon`, `color`,
  `order`)
- **`optionsSource`** — `static`, `enum`, `api`, `dependent`
- Support for grouped options, dependent/cascading dropdowns (`dependsOn`)
- **`optionLayout`** (`dropdown`, `inline`, `chips`, `buttons`, `cards`)
- **`valueKey`**, **`labelKey`** for object-based options
- Autocomplete-specific: `allowCustomValue`, debounce settings

---

## Enhanced Requirements for Lists, Sets, and Tables

When `type` is `"list"`, `"set"`, or `"table"`:

- **`itemTemplate`** — Recursive definition of a single item (via annotations)
- **`description`**, **`editable`**, **`sortable`**, **`minItems`**, **`maxItems`**
- **`actions`** — Per item actions: `add`, `edit`, `delete`, `archive`, `duplicate`, `view`, and custom actions
- **`bulkActions`** — Multi-selection actions
- **`emptyStateMessage`**, **`addButtonLabel`**
- For **`table`** specifically: `columns`, `rowSelection` (`single`/`multiple`), `pagination`, `searchable`,
  `sortableColumns`, `defaultSort`, UI style hints (`striped`, `compact`, `bordered`)

---

## MetaData JSON Structure

Define a clean, readable, hierarchical, and fully recursive JSON schema for the metadata output.

The JSON must faithfully represent all annotation-defined properties, including nested structures, collections, options,
masks, and actions.

Provide **three realistic JSON examples**:

1. Simple form with various input fields (masks, formatting, required/optional, read-only)
2. Form with rich selection inputs (static, grouped, dependent, autocomplete)
3. Complex enterprise form with nested objects + advanced Table/List configuration

---

## API Endpoints Requirements

### 1. MetaData Endpoint

- `GET /api/v1/forms/{viewName}/metadata`
- Parameters: `viewName`, optional `mode` (`create` | `edit`), optional `entityId`
- In edit mode: merge existing data with annotation-defined defaults (entity values take precedence)

### 2. Submit Endpoint

- `POST /api/v1/forms/{viewName}/submit`
- Accepts form data JSON matching the metadata structure
- Performs server-side validation (from annotations + Jakarta Validation) and returns field-mapped errors

Additional requirements: error handling, metadata caching strategy, versioning, and security considerations.

---

## Key Design Principles

- **Annotation-first** design: metadata is driven by Java annotations and reflection
- Strong extensibility and forward compatibility
- Clean separation between UI metadata and business logic
- Good balance between Java type safety and JSON flexibility for the frontend
- High readability of both annotations and generated JSON
- Performance awareness for complex nested forms and large option sets
- Future-proof for i18n, multi-step wizards, file uploads, and custom field types

---

## Suggestions & Recommendations

Include your expert suggestions for:

- Best practices for annotation design (clean, reusable, composable)
- Handling complex default values (especially for objects and lists)
- Strategy for dynamic options and dependent fields
- Caching and performance optimization of metadata generation
- Versioning strategy for views
- Support for internationalization (i18n) of labels, messages, and options
- Extensibility mechanisms for future custom field types or behaviors

---

## Output Instructions

Organize your response with the following clear sections:

1. Overall Goals
2. Annotation-Driven Approach
3. Core Field Properties
4. Enhanced Requirements for Input Fields
5. Enhanced Requirements for Options / Selection Inputs
6. Enhanced Requirements for Lists, Sets, and Tables
7. MetaData JSON Structure (with three detailed examples)
8. API Endpoints Requirements
9. Key Design Principles
10. Suggestions & Recommendations
11. Potential Challenges & Mitigation Strategies

Be exhaustive, precise, and professional. Focus on defining **what the system needs to support** and **why**. Do **not**
provide any actual Java code, classes, or implementation details.

Generate a high-quality, production-ready specification suitable for guiding a development team in building a robust
annotation-driven dynamic form system.