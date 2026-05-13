#ifndef KONAN_LIBGRAPHDATAMODEL_H
#define KONAN_LIBGRAPHDATAMODEL_H
#ifdef __cplusplus
extern "C" {
#endif
#ifdef __cplusplus
typedef bool            libgraphdatamodel_KBoolean;
#else
typedef _Bool           libgraphdatamodel_KBoolean;
#endif
typedef unsigned short     libgraphdatamodel_KChar;
typedef signed char        libgraphdatamodel_KByte;
typedef short              libgraphdatamodel_KShort;
typedef int                libgraphdatamodel_KInt;
typedef long long          libgraphdatamodel_KLong;
typedef unsigned char      libgraphdatamodel_KUByte;
typedef unsigned short     libgraphdatamodel_KUShort;
typedef unsigned int       libgraphdatamodel_KUInt;
typedef unsigned long long libgraphdatamodel_KULong;
typedef float              libgraphdatamodel_KFloat;
typedef double             libgraphdatamodel_KDouble;
typedef float __attribute__ ((__vector_size__ (16))) libgraphdatamodel_KVector128;
typedef void*              libgraphdatamodel_KNativePtr;
struct libgraphdatamodel_KType;
typedef struct libgraphdatamodel_KType libgraphdatamodel_KType;

typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlin_Byte;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlin_Short;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlin_Int;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlin_Long;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlin_Float;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlin_Double;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlin_Char;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlin_Boolean;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlin_Unit;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlin_UByte;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlin_UShort;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlin_UInt;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlin_ULong;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_GraphSpec;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlin_Function1;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_GraphSpecConfig;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_GraphModel;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_GraphSpec_Json;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_GraphSpec_Yaml;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlin_Array;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_bridge_BridgeResponse;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlin_Any;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_bridge_BridgeResponse_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_bridge_BridgeResponse_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlinx_serialization_KSerializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_codec_format_Format;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_codec_schema_SchemaElement;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_codec_format_JsonFormat;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlinx_serialization_json_Json;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_codec_format_JsonFormat_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_codec_format_YamlFormat;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_net_mamoe_yamlkt_Yaml;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_net_mamoe_yamlkt_YamlElement;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_codec_format_YamlFormat_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_codec_schema_SchemaList;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_codec_schema_SchemaMap;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlin_collections_MutableList;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlin_collections_Collection;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlin_collections_MutableIterator;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlin_collections_MutableListIterator;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_codec_schema_SchemaLiteral;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlin_collections_MutableMap;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlin_collections_Set;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlin_collections_List;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_codec_schema_SchemaPrimitive;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlin_collections_Map;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_codec_schema_SchemaNull;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_migrate_Migration;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_migrate_migration_dataModel_DataModelV2V3Migration;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_migrate_migration_dataModel_DataModelV3GraphSpecMigration;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_migrate_migration_dataModel_DataModelV3GraphSpecMigration_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_migrate_migration_dataModel_GraphSpecDataModelV3Migration;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_migrate_migration_dataModel_GraphSpecDataModelV3Migration_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_migrate_migration_dataModel_GraphSpecV3PrettyMigration;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_migrate_MigrationPath;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_display_Display;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_GraphModel_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_GraphModel_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_display_Display_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_display_Display_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_display_NodeDisplay;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_display_NodeDisplay_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_display_NodeDisplay_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_extension_BooleanValue;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_extension_BooleanValue_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_extension_BooleanValue_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_extension_DoubleValue;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_extension_DoubleValue_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_extension_DoubleValue_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_extension_ExtensionType;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_extension_ExtensionValue;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_extension_ExtensionValue_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_extension_ExtensionValueSerializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlinx_serialization_json_JsonElement;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_kotlinx_serialization_DeserializationStrategy;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_extension_Extensions;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_extension_ListValue;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_extension_ListValue_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_extension_ListValue_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_extension_LongValue;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_extension_LongValue_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_extension_LongValue_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_extension_MapValue;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_extension_MapValue_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_extension_MapValue_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_extension_StringValue;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_extension_StringValue_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_extension_StringValue_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_mapping_LabelMapping;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_mapping_LabelMapping_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_mapping_LabelMapping_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_mapping_Mapping;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_mapping_Mapping_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_mapping_MappingMode;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_mapping_MappingMode_MERGE;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_mapping_MappingMode_CREATE;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_mapping_MappingMode_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_mapping_MappingSerializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_mapping_MappingType;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_mapping_NodeMapping;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_mapping_NodeMapping_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_mapping_NodeMapping_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_mapping_PropertyMapping;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_mapping_PropertyMapping_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_mapping_PropertyMapping_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_mapping_QueryMapping;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_mapping_QueryMapping_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_mapping_QueryMapping_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_mapping_RelationshipMapping;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_mapping_TargetMapping;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_mapping_RelationshipMapping_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_mapping_RelationshipMapping_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_mapping_TargetMapping_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_mapping_TargetMapping_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_node_Labels;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_node_Labels_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_node_Labels_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_node_Node;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_node_Node_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_node_Node_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_node_NodeConstraint;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_node_NodeConstraint_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_node_NodeConstraint_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_node_NodeIndex;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_node_NodeIndex_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_node_NodeIndex_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_ANY;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_BOOLEAN;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_LIST_BOOLEAN;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_DATE;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_LIST_DATE;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_DURATION;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_LIST_DURATION;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_FLOAT32;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_LIST_FLOAT32;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_FLOAT;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_LIST_FLOAT;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_INTEGER8;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_LIST_INTEGER8;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_INTEGER16;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_LIST_INTEGER16;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_INTEGER32;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_LIST_INTEGER32;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_INTEGER;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_LIST_INTEGER;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_LOCAL_DATETIME;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_LIST_LOCAL_DATETIME;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_LOCAL_TIME;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_LIST_LOCAL_TIME;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_POINT;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_LIST_POINT;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_STRING;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_LIST_STRING;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_VECTOR_FLOAT;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_VECTOR_FLOAT32;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_VECTOR_INTEGER;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_VECTOR_INTEGER32;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_VECTOR_INTEGER16;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_VECTOR_INTEGER8;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_ZONED_DATETIME;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_LIST_ZONED_DATETIME;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_ZONED_TIME;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_LIST_ZONED_TIME;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_UUID;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Neo4jType_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Property;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Property_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_property_Property_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_relationship_Relationship;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_relationship_RelationshipTarget;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_relationship_Relationship_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_relationship_Relationship_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_relationship_RelationshipConstraint;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_relationship_RelationshipConstraint_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_relationship_RelationshipConstraint_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_relationship_RelationshipIndex;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_relationship_RelationshipIndex_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_relationship_RelationshipIndex_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_relationship_RelationshipTarget_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_relationship_RelationshipTarget_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_source_ForeignKey;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_source_ForeignKeyReference;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_source_ForeignKey_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_source_ForeignKey_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_source_ForeignKeyReference_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_source_ForeignKeyReference_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_source_Table;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_source_Table_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_source_Table_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_source_TableField;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_source_TableField_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_source_TableField_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_type_ConstraintType;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_type_ConstraintType_EXISTS;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_type_ConstraintType_KEY;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_type_ConstraintType_TYPE;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_type_ConstraintType_UNIQUE;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_type_IndexType;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_type_IndexType_FULLTEXT;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_type_IndexType_POINT;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_type_IndexType_RANGE;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_type_IndexType_TEXT;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_type_IndexType_VECTOR;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_type_IndexType_LOOKUP;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_Type;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_model_Version;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_validate_Issue;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_validate_Issue_$serializer;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_validate_Issue_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_validate_node_NodeConstraints;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_validate_node_constraint_NodeExistenceConstraint;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_validate_node_constraint_NodeTypeConstraint;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_validate_node_NodeIndexesExists;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_validate_node_NodeValidation;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_validate_relationship_RelationshipConstraints;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_validate_relationship_constraint_RelationshipExistenceConstraint;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_validate_relationship_constraint_RelationshipTypeConstraint;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_validate_relationship_RelationshipIndexes;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_validate_relationship_RelationshipNodes;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_validate_relationship_RelationshipValidation;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_validate_Validation;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_validate_ValidationTree;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_validate_Validations;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_validate_Validations_Companion;
typedef struct {
  libgraphdatamodel_KNativePtr pinned;
} libgraphdatamodel_kref_GraphSpecConfig_Builder;

extern libgraphdatamodel_KInt migrate(void* inputJson, void* inputType, void* targetType, void* targetVersion, void* outputBuffer, libgraphdatamodel_KInt bufferSize);
extern libgraphdatamodel_KInt validate(void* inputJson, void* outputBuffer, libgraphdatamodel_KInt bufferSize);

typedef struct {
  /* Service functions. */
  void (*DisposeStablePointer)(libgraphdatamodel_KNativePtr ptr);
  void (*DisposeString)(const char* string);
  libgraphdatamodel_KBoolean (*IsInstance)(libgraphdatamodel_KNativePtr ref, const libgraphdatamodel_KType* type);
  libgraphdatamodel_kref_kotlin_Byte (*createNullableByte)(libgraphdatamodel_KByte);
  libgraphdatamodel_KByte (*getNonNullValueOfByte)(libgraphdatamodel_kref_kotlin_Byte);
  libgraphdatamodel_kref_kotlin_Short (*createNullableShort)(libgraphdatamodel_KShort);
  libgraphdatamodel_KShort (*getNonNullValueOfShort)(libgraphdatamodel_kref_kotlin_Short);
  libgraphdatamodel_kref_kotlin_Int (*createNullableInt)(libgraphdatamodel_KInt);
  libgraphdatamodel_KInt (*getNonNullValueOfInt)(libgraphdatamodel_kref_kotlin_Int);
  libgraphdatamodel_kref_kotlin_Long (*createNullableLong)(libgraphdatamodel_KLong);
  libgraphdatamodel_KLong (*getNonNullValueOfLong)(libgraphdatamodel_kref_kotlin_Long);
  libgraphdatamodel_kref_kotlin_Float (*createNullableFloat)(libgraphdatamodel_KFloat);
  libgraphdatamodel_KFloat (*getNonNullValueOfFloat)(libgraphdatamodel_kref_kotlin_Float);
  libgraphdatamodel_kref_kotlin_Double (*createNullableDouble)(libgraphdatamodel_KDouble);
  libgraphdatamodel_KDouble (*getNonNullValueOfDouble)(libgraphdatamodel_kref_kotlin_Double);
  libgraphdatamodel_kref_kotlin_Char (*createNullableChar)(libgraphdatamodel_KChar);
  libgraphdatamodel_KChar (*getNonNullValueOfChar)(libgraphdatamodel_kref_kotlin_Char);
  libgraphdatamodel_kref_kotlin_Boolean (*createNullableBoolean)(libgraphdatamodel_KBoolean);
  libgraphdatamodel_KBoolean (*getNonNullValueOfBoolean)(libgraphdatamodel_kref_kotlin_Boolean);
  libgraphdatamodel_kref_kotlin_Unit (*createNullableUnit)(void);
  libgraphdatamodel_kref_kotlin_UByte (*createNullableUByte)(libgraphdatamodel_KUByte);
  libgraphdatamodel_KUByte (*getNonNullValueOfUByte)(libgraphdatamodel_kref_kotlin_UByte);
  libgraphdatamodel_kref_kotlin_UShort (*createNullableUShort)(libgraphdatamodel_KUShort);
  libgraphdatamodel_KUShort (*getNonNullValueOfUShort)(libgraphdatamodel_kref_kotlin_UShort);
  libgraphdatamodel_kref_kotlin_UInt (*createNullableUInt)(libgraphdatamodel_KUInt);
  libgraphdatamodel_KUInt (*getNonNullValueOfUInt)(libgraphdatamodel_kref_kotlin_UInt);
  libgraphdatamodel_kref_kotlin_ULong (*createNullableULong)(libgraphdatamodel_KULong);
  libgraphdatamodel_KULong (*getNonNullValueOfULong)(libgraphdatamodel_kref_kotlin_ULong);

  /* User functions. */
  struct {
    struct {
      struct {
        struct {
          libgraphdatamodel_KType* (*_type)(void);
          libgraphdatamodel_kref_GraphSpec_Json (*_instance)();
        } Json;
        struct {
          libgraphdatamodel_KType* (*_type)(void);
          libgraphdatamodel_kref_GraphSpec_Yaml (*_instance)();
        } Yaml;
        libgraphdatamodel_KType* (*_type)(void);
        libgraphdatamodel_kref_GraphSpec (*GraphSpec)(libgraphdatamodel_kref_GraphSpecConfig configuration);
        libgraphdatamodel_kref_GraphSpecConfig (*get_configuration)(libgraphdatamodel_kref_GraphSpec thiz);
        libgraphdatamodel_kref_model_GraphModel (*decodeFromString)(libgraphdatamodel_kref_GraphSpec thiz, const char* content, const char* type);
        const char* (*encodeToString)(libgraphdatamodel_kref_GraphSpec thiz, libgraphdatamodel_kref_model_GraphModel model, const char* targetType, const char* targetVersion);
      } GraphSpec;
      struct {
        struct {
          struct {
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_bridge_BridgeResponse_$serializer (*_instance)();
            libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_bridge_BridgeResponse_$serializer thiz);
            libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_bridge_BridgeResponse_$serializer thiz);
            libgraphdatamodel_kref_bridge_BridgeResponse (*deserialize)(libgraphdatamodel_kref_bridge_BridgeResponse_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
            void (*serialize)(libgraphdatamodel_kref_bridge_BridgeResponse_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_bridge_BridgeResponse value);
          } $serializer;
          struct {
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_bridge_BridgeResponse_Companion (*_instance)();
            libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_bridge_BridgeResponse_Companion thiz);
          } Companion;
          libgraphdatamodel_KType* (*_type)(void);
          libgraphdatamodel_kref_bridge_BridgeResponse (*BridgeResponse)(const char* data, const char* error);
          const char* (*get_data)(libgraphdatamodel_kref_bridge_BridgeResponse thiz);
          const char* (*get_error)(libgraphdatamodel_kref_bridge_BridgeResponse thiz);
          const char* (*component1)(libgraphdatamodel_kref_bridge_BridgeResponse thiz);
          const char* (*component2)(libgraphdatamodel_kref_bridge_BridgeResponse thiz);
          libgraphdatamodel_kref_bridge_BridgeResponse (*copy)(libgraphdatamodel_kref_bridge_BridgeResponse thiz, const char* data, const char* error);
          libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_bridge_BridgeResponse thiz, libgraphdatamodel_kref_kotlin_Any other);
          libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_bridge_BridgeResponse thiz);
          const char* (*toString)(libgraphdatamodel_kref_bridge_BridgeResponse thiz);
        } BridgeResponse;
        libgraphdatamodel_KInt (*invokeBridge)(libgraphdatamodel_kref_kotlin_Array input, void* outputBuffer, libgraphdatamodel_KInt bufferSize, libgraphdatamodel_kref_kotlin_Function1 action);
        libgraphdatamodel_KInt (*migrate_)(void* inputJson, void* inputType, void* targetType, void* targetVersion, void* outputBuffer, libgraphdatamodel_KInt bufferSize);
        libgraphdatamodel_KInt (*validate_)(void* inputJson, void* outputBuffer, libgraphdatamodel_KInt bufferSize);
      } bridge;
      struct {
        struct {
          struct {
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_GraphModel (*decodeFromSchema)(libgraphdatamodel_kref_codec_format_Format thiz, libgraphdatamodel_kref_codec_schema_SchemaElement element);
            libgraphdatamodel_kref_codec_schema_SchemaElement (*decodeFromString)(libgraphdatamodel_kref_codec_format_Format thiz, const char* string);
            libgraphdatamodel_kref_codec_schema_SchemaElement (*encodeToSchema)(libgraphdatamodel_kref_codec_format_Format thiz, libgraphdatamodel_kref_model_GraphModel model);
            const char* (*encodeToString)(libgraphdatamodel_kref_codec_format_Format thiz, libgraphdatamodel_kref_codec_schema_SchemaElement element);
          } Format;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_codec_format_JsonFormat_Companion (*_instance)();
              libgraphdatamodel_kref_codec_format_JsonFormat (*get_default)(libgraphdatamodel_kref_codec_format_JsonFormat_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_codec_format_JsonFormat (*JsonFormat)(libgraphdatamodel_kref_kotlinx_serialization_json_Json json);
            libgraphdatamodel_kref_model_GraphModel (*decodeFromSchema)(libgraphdatamodel_kref_codec_format_JsonFormat thiz, libgraphdatamodel_kref_codec_schema_SchemaElement element);
            libgraphdatamodel_kref_codec_schema_SchemaElement (*decodeFromString)(libgraphdatamodel_kref_codec_format_JsonFormat thiz, const char* string);
            libgraphdatamodel_kref_codec_schema_SchemaElement (*encodeToSchema)(libgraphdatamodel_kref_codec_format_JsonFormat thiz, libgraphdatamodel_kref_model_GraphModel model);
            const char* (*encodeToString)(libgraphdatamodel_kref_codec_format_JsonFormat thiz, libgraphdatamodel_kref_codec_schema_SchemaElement element);
          } JsonFormat;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_codec_format_YamlFormat_Companion (*_instance)();
              libgraphdatamodel_kref_codec_format_YamlFormat (*get_default)(libgraphdatamodel_kref_codec_format_YamlFormat_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_codec_format_YamlFormat (*YamlFormat)(libgraphdatamodel_kref_net_mamoe_yamlkt_Yaml yaml, libgraphdatamodel_kref_codec_format_JsonFormat json);
            libgraphdatamodel_kref_model_GraphModel (*decodeFromSchema)(libgraphdatamodel_kref_codec_format_YamlFormat thiz, libgraphdatamodel_kref_codec_schema_SchemaElement element);
            libgraphdatamodel_kref_codec_schema_SchemaElement (*decodeFromString)(libgraphdatamodel_kref_codec_format_YamlFormat thiz, const char* string);
            libgraphdatamodel_kref_codec_schema_SchemaElement (*encodeToSchema)(libgraphdatamodel_kref_codec_format_YamlFormat thiz, libgraphdatamodel_kref_model_GraphModel model);
            const char* (*encodeToString)(libgraphdatamodel_kref_codec_format_YamlFormat thiz, libgraphdatamodel_kref_codec_schema_SchemaElement element);
            libgraphdatamodel_kref_codec_schema_SchemaElement (*schemaElement)(libgraphdatamodel_kref_codec_format_YamlFormat thiz, libgraphdatamodel_kref_net_mamoe_yamlkt_YamlElement yaml, const char* parent);
            libgraphdatamodel_kref_net_mamoe_yamlkt_YamlElement (*toYaml)(libgraphdatamodel_kref_codec_format_YamlFormat thiz, libgraphdatamodel_kref_codec_schema_SchemaElement thiz1);
          } YamlFormat;
        } format;
        struct {
          struct {
            libgraphdatamodel_KType* (*_type)(void);
            const char* (*get_path)(libgraphdatamodel_kref_codec_schema_SchemaElement thiz);
            libgraphdatamodel_kref_codec_schema_SchemaElement (*repath)(libgraphdatamodel_kref_codec_schema_SchemaElement thiz, const char* newPath);
            const char* (*toString)(libgraphdatamodel_kref_codec_schema_SchemaElement thiz);
          } SchemaElement;
          struct {
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_codec_schema_SchemaList (*SchemaList)(libgraphdatamodel_kref_kotlin_collections_MutableList content, const char* path);
            libgraphdatamodel_kref_kotlin_collections_MutableList (*get_content)(libgraphdatamodel_kref_codec_schema_SchemaList thiz);
            const char* (*get_path)(libgraphdatamodel_kref_codec_schema_SchemaList thiz);
            libgraphdatamodel_KInt (*get_size)(libgraphdatamodel_kref_codec_schema_SchemaList thiz);
            libgraphdatamodel_KBoolean (*add)(libgraphdatamodel_kref_codec_schema_SchemaList thiz, libgraphdatamodel_kref_codec_schema_SchemaElement element);
            void (*add_)(libgraphdatamodel_kref_codec_schema_SchemaList thiz, libgraphdatamodel_KInt index, libgraphdatamodel_kref_codec_schema_SchemaElement element);
            libgraphdatamodel_KBoolean (*addAll)(libgraphdatamodel_kref_codec_schema_SchemaList thiz, libgraphdatamodel_kref_kotlin_collections_Collection elements);
            libgraphdatamodel_KBoolean (*addAll_)(libgraphdatamodel_kref_codec_schema_SchemaList thiz, libgraphdatamodel_KInt index, libgraphdatamodel_kref_kotlin_collections_Collection elements);
            void (*clear)(libgraphdatamodel_kref_codec_schema_SchemaList thiz);
            libgraphdatamodel_kref_kotlin_collections_MutableList (*component1)(libgraphdatamodel_kref_codec_schema_SchemaList thiz);
            const char* (*component2)(libgraphdatamodel_kref_codec_schema_SchemaList thiz);
            libgraphdatamodel_KBoolean (*contains)(libgraphdatamodel_kref_codec_schema_SchemaList thiz, libgraphdatamodel_kref_codec_schema_SchemaElement element);
            libgraphdatamodel_KBoolean (*containsAll)(libgraphdatamodel_kref_codec_schema_SchemaList thiz, libgraphdatamodel_kref_kotlin_collections_Collection elements);
            libgraphdatamodel_kref_codec_schema_SchemaList (*copy)(libgraphdatamodel_kref_codec_schema_SchemaList thiz, libgraphdatamodel_kref_kotlin_collections_MutableList content, const char* path);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_codec_schema_SchemaList thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_kref_codec_schema_SchemaElement (*get)(libgraphdatamodel_kref_codec_schema_SchemaList thiz, libgraphdatamodel_KInt index);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_codec_schema_SchemaList thiz);
            libgraphdatamodel_KInt (*indexOf)(libgraphdatamodel_kref_codec_schema_SchemaList thiz, libgraphdatamodel_kref_codec_schema_SchemaElement element);
            libgraphdatamodel_KBoolean (*isEmpty)(libgraphdatamodel_kref_codec_schema_SchemaList thiz);
            libgraphdatamodel_kref_kotlin_collections_MutableIterator (*iterator)(libgraphdatamodel_kref_codec_schema_SchemaList thiz);
            libgraphdatamodel_KInt (*lastIndexOf)(libgraphdatamodel_kref_codec_schema_SchemaList thiz, libgraphdatamodel_kref_codec_schema_SchemaElement element);
            libgraphdatamodel_kref_kotlin_collections_MutableListIterator (*listIterator)(libgraphdatamodel_kref_codec_schema_SchemaList thiz);
            libgraphdatamodel_kref_kotlin_collections_MutableListIterator (*listIterator_)(libgraphdatamodel_kref_codec_schema_SchemaList thiz, libgraphdatamodel_KInt index);
            libgraphdatamodel_KBoolean (*remove)(libgraphdatamodel_kref_codec_schema_SchemaList thiz, libgraphdatamodel_kref_codec_schema_SchemaElement element);
            libgraphdatamodel_KBoolean (*removeAll)(libgraphdatamodel_kref_codec_schema_SchemaList thiz, libgraphdatamodel_kref_kotlin_collections_Collection elements);
            libgraphdatamodel_kref_codec_schema_SchemaElement (*removeAt)(libgraphdatamodel_kref_codec_schema_SchemaList thiz, libgraphdatamodel_KInt index);
            libgraphdatamodel_kref_codec_schema_SchemaList (*repath)(libgraphdatamodel_kref_codec_schema_SchemaList thiz, const char* newPath);
            libgraphdatamodel_KBoolean (*retainAll)(libgraphdatamodel_kref_codec_schema_SchemaList thiz, libgraphdatamodel_kref_kotlin_collections_Collection elements);
            libgraphdatamodel_kref_codec_schema_SchemaElement (*set)(libgraphdatamodel_kref_codec_schema_SchemaList thiz, libgraphdatamodel_KInt index, libgraphdatamodel_kref_codec_schema_SchemaElement element);
            libgraphdatamodel_kref_kotlin_collections_MutableList (*subList)(libgraphdatamodel_kref_codec_schema_SchemaList thiz, libgraphdatamodel_KInt fromIndex, libgraphdatamodel_KInt toIndex);
            const char* (*toString)(libgraphdatamodel_kref_codec_schema_SchemaList thiz);
          } SchemaList;
          struct {
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_codec_schema_SchemaLiteral (*SchemaLiteral)(const char* string, const char* path, libgraphdatamodel_KBoolean isString);
            libgraphdatamodel_KBoolean (*get_isString)(libgraphdatamodel_kref_codec_schema_SchemaLiteral thiz);
            const char* (*get_path)(libgraphdatamodel_kref_codec_schema_SchemaLiteral thiz);
            const char* (*get_string)(libgraphdatamodel_kref_codec_schema_SchemaLiteral thiz);
            const char* (*component1)(libgraphdatamodel_kref_codec_schema_SchemaLiteral thiz);
            const char* (*component2)(libgraphdatamodel_kref_codec_schema_SchemaLiteral thiz);
            libgraphdatamodel_KBoolean (*component3)(libgraphdatamodel_kref_codec_schema_SchemaLiteral thiz);
            libgraphdatamodel_kref_codec_schema_SchemaLiteral (*copy)(libgraphdatamodel_kref_codec_schema_SchemaLiteral thiz, const char* string, const char* path, libgraphdatamodel_KBoolean isString);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_codec_schema_SchemaLiteral thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_codec_schema_SchemaLiteral thiz);
            libgraphdatamodel_kref_codec_schema_SchemaLiteral (*repath)(libgraphdatamodel_kref_codec_schema_SchemaLiteral thiz, const char* newPath);
            const char* (*toString)(libgraphdatamodel_kref_codec_schema_SchemaLiteral thiz);
          } SchemaLiteral;
          struct {
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_codec_schema_SchemaMap (*SchemaMap)(libgraphdatamodel_kref_kotlin_collections_MutableMap content);
            libgraphdatamodel_kref_codec_schema_SchemaMap (*SchemaMap_)(libgraphdatamodel_kref_kotlin_collections_MutableMap content, const char* path);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*get_content)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*get_entries)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*get_keys)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz);
            const char* (*get_path)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz);
            libgraphdatamodel_KInt (*get_size)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz);
            libgraphdatamodel_kref_kotlin_collections_Collection (*get_values)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz);
            libgraphdatamodel_KBoolean (*bool_)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, const char* key);
            libgraphdatamodel_kref_kotlin_Boolean (*boolOrNull)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, const char* key);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*component1)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz);
            const char* (*component2)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz);
            libgraphdatamodel_KBoolean (*containsKey)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, const char* key);
            libgraphdatamodel_KBoolean (*containsValue)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, libgraphdatamodel_kref_codec_schema_SchemaElement value);
            libgraphdatamodel_kref_codec_schema_SchemaMap (*copy)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, libgraphdatamodel_kref_kotlin_collections_MutableMap content, const char* path);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_kref_codec_schema_SchemaElement (*get)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, const char* key);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz);
            libgraphdatamodel_kref_kotlin_Int (*int_)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, const char* key);
            libgraphdatamodel_kref_kotlin_Int (*intOrNull)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, const char* key);
            libgraphdatamodel_KBoolean (*isEmpty)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz);
            libgraphdatamodel_kref_codec_schema_SchemaList (*list)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, const char* key);
            libgraphdatamodel_kref_kotlin_collections_List (*listOfMaps)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, const char* key);
            libgraphdatamodel_kref_kotlin_collections_List (*listOfMapsOrNull)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, const char* key);
            libgraphdatamodel_kref_codec_schema_SchemaList (*listOrNull)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, const char* key);
            libgraphdatamodel_kref_codec_schema_SchemaPrimitive (*literal)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, const char* key);
            libgraphdatamodel_kref_codec_schema_SchemaLiteral (*literalOrNull)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, const char* key);
            libgraphdatamodel_kref_codec_schema_SchemaMap (*map)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, const char* key);
            libgraphdatamodel_kref_kotlin_collections_Map (*mapOfMaps)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, const char* key);
            libgraphdatamodel_kref_kotlin_collections_Map (*mapOfMapsOrNull)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, const char* key);
            libgraphdatamodel_kref_codec_schema_SchemaMap (*mapOrNull)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, const char* key);
            libgraphdatamodel_kref_codec_schema_SchemaMap (*mapOrPut)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, const char* key);
            libgraphdatamodel_kref_codec_schema_SchemaElement (*put)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, const char* key, libgraphdatamodel_kref_codec_schema_SchemaElement value);
            void (*putAll)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, libgraphdatamodel_kref_kotlin_collections_Map from);
            libgraphdatamodel_kref_codec_schema_SchemaElement (*remove)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, const char* key);
            libgraphdatamodel_kref_codec_schema_SchemaMap (*removeMap)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, const char* key);
            libgraphdatamodel_kref_codec_schema_SchemaMap (*repath)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, const char* newPath);
            void (*set)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, const char* key, libgraphdatamodel_kref_kotlin_Any value);
            const char* (*string)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, const char* key);
            const char* (*stringOrNull)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz, const char* key);
            const char* (*toString)(libgraphdatamodel_kref_codec_schema_SchemaMap thiz);
          } SchemaMap;
          struct {
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_codec_schema_SchemaNull (*SchemaNull)(const char* path);
            libgraphdatamodel_KBoolean (*get_isString)(libgraphdatamodel_kref_codec_schema_SchemaNull thiz);
            const char* (*get_path)(libgraphdatamodel_kref_codec_schema_SchemaNull thiz);
            const char* (*get_string)(libgraphdatamodel_kref_codec_schema_SchemaNull thiz);
            const char* (*component1)(libgraphdatamodel_kref_codec_schema_SchemaNull thiz);
            libgraphdatamodel_kref_codec_schema_SchemaNull (*copy)(libgraphdatamodel_kref_codec_schema_SchemaNull thiz, const char* path);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_codec_schema_SchemaNull thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_codec_schema_SchemaNull thiz);
            libgraphdatamodel_kref_codec_schema_SchemaNull (*repath)(libgraphdatamodel_kref_codec_schema_SchemaNull thiz, const char* newPath);
            const char* (*toString)(libgraphdatamodel_kref_codec_schema_SchemaNull thiz);
          } SchemaNull;
          struct {
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_codec_schema_SchemaPrimitive (*SchemaPrimitive)();
            libgraphdatamodel_KBoolean (*get_isString)(libgraphdatamodel_kref_codec_schema_SchemaPrimitive thiz);
            const char* (*get_string)(libgraphdatamodel_kref_codec_schema_SchemaPrimitive thiz);
          } SchemaPrimitive;
          libgraphdatamodel_kref_codec_schema_SchemaElement (*toSchemaElement)(libgraphdatamodel_kref_kotlin_Any thiz, const char* path);
          libgraphdatamodel_kref_codec_schema_SchemaList (*schemaListOf)(libgraphdatamodel_kref_kotlin_Array elements);
          libgraphdatamodel_kref_codec_schema_SchemaMap (*buildSchemaMap)(libgraphdatamodel_kref_kotlin_Function1 block);
          libgraphdatamodel_kref_codec_schema_SchemaMap (*schemaMapOf)(libgraphdatamodel_kref_kotlin_Array pairs);
        } schema;
      } codec;
      struct {
        struct {
          libgraphdatamodel_KType* (*_type)(void);
          libgraphdatamodel_kref_migrate_Migration (*Migration)(const char* fromType, const char* from, const char* toType, const char* to);
          const char* (*get_from)(libgraphdatamodel_kref_migrate_Migration thiz);
          const char* (*get_fromKey)(libgraphdatamodel_kref_migrate_Migration thiz);
          const char* (*get_fromType)(libgraphdatamodel_kref_migrate_Migration thiz);
          const char* (*get_to)(libgraphdatamodel_kref_migrate_Migration thiz);
          const char* (*get_toKey)(libgraphdatamodel_kref_migrate_Migration thiz);
          const char* (*get_toType)(libgraphdatamodel_kref_migrate_Migration thiz);
          libgraphdatamodel_kref_codec_schema_SchemaMap (*migrate)(libgraphdatamodel_kref_migrate_Migration thiz, libgraphdatamodel_kref_codec_schema_SchemaMap schema);
        } Migration;
        struct {
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_migrate_migration_dataModel_DataModelV2V3Migration (*DataModelV2V3Migration)(const char* version);
              libgraphdatamodel_kref_codec_schema_SchemaMap (*migrate)(libgraphdatamodel_kref_migrate_migration_dataModel_DataModelV2V3Migration thiz, libgraphdatamodel_kref_codec_schema_SchemaMap schema);
            } DataModelV2V3Migration;
            struct {
              struct {
                libgraphdatamodel_KType* (*_type)(void);
                libgraphdatamodel_kref_migrate_migration_dataModel_DataModelV3GraphSpecMigration_Companion (*_instance)();
              } Companion;
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_migrate_migration_dataModel_DataModelV3GraphSpecMigration (*DataModelV3GraphSpecMigration)();
              libgraphdatamodel_kref_codec_schema_SchemaMap (*migrate)(libgraphdatamodel_kref_migrate_migration_dataModel_DataModelV3GraphSpecMigration thiz, libgraphdatamodel_kref_codec_schema_SchemaMap schema);
            } DataModelV3GraphSpecMigration;
            struct {
              struct {
                libgraphdatamodel_KType* (*_type)(void);
                libgraphdatamodel_kref_migrate_migration_dataModel_GraphSpecDataModelV3Migration_Companion (*_instance)();
              } Companion;
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_migrate_migration_dataModel_GraphSpecDataModelV3Migration (*GraphSpecDataModelV3Migration)();
              libgraphdatamodel_kref_codec_schema_SchemaMap (*migrate)(libgraphdatamodel_kref_migrate_migration_dataModel_GraphSpecDataModelV3Migration thiz, libgraphdatamodel_kref_codec_schema_SchemaMap schema);
            } GraphSpecDataModelV3Migration;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_migrate_migration_dataModel_GraphSpecV3PrettyMigration (*GraphSpecV3PrettyMigration)();
              libgraphdatamodel_kref_codec_schema_SchemaMap (*migrate)(libgraphdatamodel_kref_migrate_migration_dataModel_GraphSpecV3PrettyMigration thiz, libgraphdatamodel_kref_codec_schema_SchemaMap schema);
              void (*sort)(libgraphdatamodel_kref_migrate_migration_dataModel_GraphSpecV3PrettyMigration thiz, libgraphdatamodel_kref_codec_schema_SchemaMap schema);
              void (*tidyConstraints)(libgraphdatamodel_kref_migrate_migration_dataModel_GraphSpecV3PrettyMigration thiz, libgraphdatamodel_kref_codec_schema_SchemaMap schema);
            } GraphSpecV3PrettyMigration;
          } dataModel;
        } migration;
        struct {
          libgraphdatamodel_KType* (*_type)(void);
          libgraphdatamodel_kref_migrate_MigrationPath (*MigrationPath)(libgraphdatamodel_kref_kotlin_collections_Map migrations);
          libgraphdatamodel_kref_kotlin_collections_Map (*get_migrations)(libgraphdatamodel_kref_migrate_MigrationPath thiz);
          libgraphdatamodel_kref_kotlin_collections_List (*findPath)(libgraphdatamodel_kref_migrate_MigrationPath thiz, const char* from, const char* to);
          libgraphdatamodel_kref_codec_schema_SchemaMap (*migrate)(libgraphdatamodel_kref_migrate_MigrationPath thiz, libgraphdatamodel_kref_codec_schema_SchemaMap schema, const char* type, const char* targetVersion, const char* targetType);
        } MigrationPath;
      } migrate;
      struct {
        struct {
          struct {
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_GraphModel_Companion (*_instance)();
            libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_GraphModel_Companion thiz);
            libgraphdatamodel_kref_kotlin_collections_List (*validate)(libgraphdatamodel_kref_model_GraphModel_Companion thiz, libgraphdatamodel_kref_model_GraphModel model, libgraphdatamodel_kref_kotlin_collections_List validators);
          } Companion;
          struct {
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_GraphModel_$serializer (*_instance)();
            libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_GraphModel_$serializer thiz);
            libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_GraphModel_$serializer thiz);
            libgraphdatamodel_kref_model_GraphModel (*deserialize)(libgraphdatamodel_kref_model_GraphModel_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
            void (*serialize)(libgraphdatamodel_kref_model_GraphModel_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_GraphModel value);
          } $serializer;
          libgraphdatamodel_KType* (*_type)(void);
          libgraphdatamodel_kref_model_GraphModel (*GraphModel)(const char* version, libgraphdatamodel_kref_kotlin_collections_Map nodes, libgraphdatamodel_kref_kotlin_collections_Map relationships, libgraphdatamodel_kref_kotlin_collections_Map tables, libgraphdatamodel_kref_kotlin_collections_List mappings, libgraphdatamodel_kref_model_display_Display display);
          libgraphdatamodel_kref_model_display_Display (*get_display)(libgraphdatamodel_kref_model_GraphModel thiz);
          libgraphdatamodel_kref_kotlin_collections_List (*get_mappings)(libgraphdatamodel_kref_model_GraphModel thiz);
          libgraphdatamodel_kref_kotlin_collections_Map (*get_nodes)(libgraphdatamodel_kref_model_GraphModel thiz);
          libgraphdatamodel_kref_kotlin_collections_Map (*get_relationships)(libgraphdatamodel_kref_model_GraphModel thiz);
          libgraphdatamodel_kref_kotlin_collections_Map (*get_tables)(libgraphdatamodel_kref_model_GraphModel thiz);
          const char* (*get_version)(libgraphdatamodel_kref_model_GraphModel thiz);
          const char* (*component1)(libgraphdatamodel_kref_model_GraphModel thiz);
          libgraphdatamodel_kref_kotlin_collections_Map (*component2)(libgraphdatamodel_kref_model_GraphModel thiz);
          libgraphdatamodel_kref_kotlin_collections_Map (*component3)(libgraphdatamodel_kref_model_GraphModel thiz);
          libgraphdatamodel_kref_kotlin_collections_Map (*component4)(libgraphdatamodel_kref_model_GraphModel thiz);
          libgraphdatamodel_kref_kotlin_collections_List (*component5)(libgraphdatamodel_kref_model_GraphModel thiz);
          libgraphdatamodel_kref_model_display_Display (*component6)(libgraphdatamodel_kref_model_GraphModel thiz);
          libgraphdatamodel_kref_model_GraphModel (*copy)(libgraphdatamodel_kref_model_GraphModel thiz, const char* version, libgraphdatamodel_kref_kotlin_collections_Map nodes, libgraphdatamodel_kref_kotlin_collections_Map relationships, libgraphdatamodel_kref_kotlin_collections_Map tables, libgraphdatamodel_kref_kotlin_collections_List mappings, libgraphdatamodel_kref_model_display_Display display);
          libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_GraphModel thiz, libgraphdatamodel_kref_kotlin_Any other);
          libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_GraphModel thiz);
          const char* (*toString)(libgraphdatamodel_kref_model_GraphModel thiz);
          libgraphdatamodel_kref_kotlin_collections_List (*validate)(libgraphdatamodel_kref_model_GraphModel thiz, libgraphdatamodel_kref_kotlin_collections_List validators);
        } GraphModel;
        struct {
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_display_Display_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_display_Display_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_display_Display_$serializer thiz);
              libgraphdatamodel_kref_model_display_Display (*deserialize)(libgraphdatamodel_kref_model_display_Display_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_display_Display_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_display_Display value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_display_Display_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_display_Display_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_display_Display (*Display)(libgraphdatamodel_kref_kotlin_collections_Map nodes);
            libgraphdatamodel_kref_kotlin_collections_Map (*get_nodes)(libgraphdatamodel_kref_model_display_Display thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*component1)(libgraphdatamodel_kref_model_display_Display thiz);
            libgraphdatamodel_kref_model_display_Display (*copy)(libgraphdatamodel_kref_model_display_Display thiz, libgraphdatamodel_kref_kotlin_collections_Map nodes);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_display_Display thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_display_Display thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_display_Display thiz);
          } Display;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_display_NodeDisplay_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_display_NodeDisplay_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_display_NodeDisplay_$serializer thiz);
              libgraphdatamodel_kref_model_display_NodeDisplay (*deserialize)(libgraphdatamodel_kref_model_display_NodeDisplay_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_display_NodeDisplay_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_display_NodeDisplay value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_display_NodeDisplay_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_display_NodeDisplay_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_display_NodeDisplay (*NodeDisplay)(libgraphdatamodel_KDouble x, libgraphdatamodel_KDouble y, libgraphdatamodel_kref_kotlin_collections_MutableMap extensions);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*get_extensions)(libgraphdatamodel_kref_model_display_NodeDisplay thiz);
            libgraphdatamodel_KDouble (*get_x)(libgraphdatamodel_kref_model_display_NodeDisplay thiz);
            libgraphdatamodel_KDouble (*get_y)(libgraphdatamodel_kref_model_display_NodeDisplay thiz);
            libgraphdatamodel_KDouble (*component1)(libgraphdatamodel_kref_model_display_NodeDisplay thiz);
            libgraphdatamodel_KDouble (*component2)(libgraphdatamodel_kref_model_display_NodeDisplay thiz);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*component3)(libgraphdatamodel_kref_model_display_NodeDisplay thiz);
            libgraphdatamodel_kref_model_display_NodeDisplay (*copy)(libgraphdatamodel_kref_model_display_NodeDisplay thiz, libgraphdatamodel_KDouble x, libgraphdatamodel_KDouble y, libgraphdatamodel_kref_kotlin_collections_MutableMap extensions);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_display_NodeDisplay thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_display_NodeDisplay thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_display_NodeDisplay thiz);
          } NodeDisplay;
        } display;
        struct {
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_extension_BooleanValue_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_extension_BooleanValue_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_extension_BooleanValue_$serializer thiz);
              libgraphdatamodel_kref_model_extension_BooleanValue (*deserialize)(libgraphdatamodel_kref_model_extension_BooleanValue_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_extension_BooleanValue_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_extension_BooleanValue value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_extension_BooleanValue_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_extension_BooleanValue_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_extension_BooleanValue (*BooleanValue)(libgraphdatamodel_KBoolean value);
            libgraphdatamodel_KBoolean (*get_value)(libgraphdatamodel_kref_model_extension_BooleanValue thiz);
            libgraphdatamodel_KBoolean (*component1)(libgraphdatamodel_kref_model_extension_BooleanValue thiz);
            libgraphdatamodel_kref_model_extension_BooleanValue (*copy)(libgraphdatamodel_kref_model_extension_BooleanValue thiz, libgraphdatamodel_KBoolean value);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_extension_BooleanValue thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_extension_BooleanValue thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_extension_BooleanValue thiz);
          } BooleanValue;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_extension_DoubleValue_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_extension_DoubleValue_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_extension_DoubleValue_$serializer thiz);
              libgraphdatamodel_kref_model_extension_DoubleValue (*deserialize)(libgraphdatamodel_kref_model_extension_DoubleValue_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_extension_DoubleValue_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_extension_DoubleValue value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_extension_DoubleValue_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_extension_DoubleValue_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_extension_DoubleValue (*DoubleValue)(libgraphdatamodel_KDouble value);
            libgraphdatamodel_KDouble (*get_value)(libgraphdatamodel_kref_model_extension_DoubleValue thiz);
            libgraphdatamodel_KDouble (*component1)(libgraphdatamodel_kref_model_extension_DoubleValue thiz);
            libgraphdatamodel_kref_model_extension_DoubleValue (*copy)(libgraphdatamodel_kref_model_extension_DoubleValue thiz, libgraphdatamodel_KDouble value);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_extension_DoubleValue thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_extension_DoubleValue thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_extension_DoubleValue thiz);
          } DoubleValue;
          struct {
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_extension_ExtensionType (*_instance)();
            const char* (*get_BOOLEAN)(libgraphdatamodel_kref_model_extension_ExtensionType thiz);
            const char* (*get_DOUBLE)(libgraphdatamodel_kref_model_extension_ExtensionType thiz);
            const char* (*get_LIST)(libgraphdatamodel_kref_model_extension_ExtensionType thiz);
            const char* (*get_LONG)(libgraphdatamodel_kref_model_extension_ExtensionType thiz);
            const char* (*get_MAP)(libgraphdatamodel_kref_model_extension_ExtensionType thiz);
            const char* (*get_STRING)(libgraphdatamodel_kref_model_extension_ExtensionType thiz);
          } ExtensionType;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_extension_ExtensionValue_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_extension_ExtensionValue_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_extension_ExtensionValue (*ExtensionValue)();
            libgraphdatamodel_kref_kotlin_Boolean (*get_asBoolean)(libgraphdatamodel_kref_model_extension_ExtensionValue thiz);
            libgraphdatamodel_kref_kotlin_Double (*get_asDouble)(libgraphdatamodel_kref_model_extension_ExtensionValue thiz);
            libgraphdatamodel_kref_kotlin_collections_List (*get_asList)(libgraphdatamodel_kref_model_extension_ExtensionValue thiz);
            libgraphdatamodel_kref_kotlin_Long (*get_asLong)(libgraphdatamodel_kref_model_extension_ExtensionValue thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*get_asMap)(libgraphdatamodel_kref_model_extension_ExtensionValue thiz);
            const char* (*get_asString)(libgraphdatamodel_kref_model_extension_ExtensionValue thiz);
          } ExtensionValue;
          struct {
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_extension_ExtensionValueSerializer (*_instance)();
            libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_extension_ExtensionValueSerializer thiz);
            libgraphdatamodel_kref_kotlinx_serialization_DeserializationStrategy (*selectDeserializer)(libgraphdatamodel_kref_model_extension_ExtensionValueSerializer thiz, libgraphdatamodel_kref_kotlinx_serialization_json_JsonElement element);
          } ExtensionValueSerializer;
          struct {
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*get_extensions)(libgraphdatamodel_kref_model_extension_Extensions thiz);
            libgraphdatamodel_KBoolean (*contains)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* key);
            libgraphdatamodel_kref_kotlin_Boolean (*getBoolean)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* section, const char* key);
            libgraphdatamodel_kref_kotlin_Boolean (*getBoolean_)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* key);
            libgraphdatamodel_kref_kotlin_collections_List (*getBooleanList)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* section, const char* key);
            libgraphdatamodel_kref_kotlin_collections_Map (*getBooleanMap)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* section, const char* key);
            libgraphdatamodel_kref_kotlin_Double (*getDouble)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* section, const char* key);
            libgraphdatamodel_kref_kotlin_Double (*getDouble_)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* key);
            libgraphdatamodel_kref_kotlin_collections_List (*getDoubleList)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* section, const char* key);
            libgraphdatamodel_kref_kotlin_collections_Map (*getDoubleMap)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* section, const char* key);
            libgraphdatamodel_kref_kotlin_Float (*getFloat)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* section, const char* key);
            libgraphdatamodel_kref_kotlin_Float (*getFloat_)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* key);
            libgraphdatamodel_kref_kotlin_collections_List (*getFloatList)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* section, const char* key);
            libgraphdatamodel_kref_kotlin_collections_Map (*getFloatMap)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* section, const char* key);
            libgraphdatamodel_kref_kotlin_Int (*getInt)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* section, const char* key);
            libgraphdatamodel_kref_kotlin_Int (*getInt_)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* key);
            libgraphdatamodel_kref_kotlin_collections_List (*getIntList)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* section, const char* key);
            libgraphdatamodel_kref_kotlin_collections_Map (*getIntMap)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* section, const char* key);
            libgraphdatamodel_kref_kotlin_collections_List (*getList)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* section, const char* key);
            libgraphdatamodel_kref_kotlin_Long (*getLong)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* section, const char* key);
            libgraphdatamodel_kref_kotlin_Long (*getLong_)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* key);
            libgraphdatamodel_kref_kotlin_collections_List (*getLongList)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* section, const char* key);
            libgraphdatamodel_kref_kotlin_collections_Map (*getLongMap)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* section, const char* key);
            libgraphdatamodel_kref_kotlin_collections_Map (*getMap)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* section, const char* key);
            const char* (*getString)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* section, const char* key);
            const char* (*getString_)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* key);
            libgraphdatamodel_kref_kotlin_collections_List (*getStringList)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* section, const char* key);
            libgraphdatamodel_kref_kotlin_collections_Map (*getStringMap)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* section, const char* key);
            void (*set)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* section, const char* key, const char* value);
            void (*set_)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* section, const char* key, libgraphdatamodel_KBoolean value);
            void (*set__)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* section, const char* key, libgraphdatamodel_KInt value);
            void (*set___)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* section, const char* key, libgraphdatamodel_KLong value);
            void (*set____)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* section, const char* key, libgraphdatamodel_KFloat value);
            void (*set_____)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* section, const char* key, libgraphdatamodel_KDouble value);
            void (*set______)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* key, const char* value);
            void (*set_______)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* key, libgraphdatamodel_KBoolean value);
            void (*set________)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* key, libgraphdatamodel_KInt value);
            void (*set_________)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* key, libgraphdatamodel_KLong value);
            void (*set__________)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* key, libgraphdatamodel_KFloat value);
            void (*set___________)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* key, libgraphdatamodel_KDouble value);
            void (*setIntMap)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* key, libgraphdatamodel_kref_kotlin_collections_Map value);
            void (*setList)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* key, libgraphdatamodel_kref_kotlin_collections_List value);
            void (*setStringMap)(libgraphdatamodel_kref_model_extension_Extensions thiz, const char* key, libgraphdatamodel_kref_kotlin_collections_Map value);
          } Extensions;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_extension_ListValue_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_extension_ListValue_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_extension_ListValue_$serializer thiz);
              libgraphdatamodel_kref_model_extension_ListValue (*deserialize)(libgraphdatamodel_kref_model_extension_ListValue_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_extension_ListValue_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_extension_ListValue value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_extension_ListValue_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_extension_ListValue_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_extension_ListValue (*ListValue)(libgraphdatamodel_kref_kotlin_collections_MutableList value);
            libgraphdatamodel_kref_kotlin_collections_MutableList (*get_value)(libgraphdatamodel_kref_model_extension_ListValue thiz);
            libgraphdatamodel_kref_kotlin_collections_MutableList (*component1)(libgraphdatamodel_kref_model_extension_ListValue thiz);
            libgraphdatamodel_kref_model_extension_ListValue (*copy)(libgraphdatamodel_kref_model_extension_ListValue thiz, libgraphdatamodel_kref_kotlin_collections_MutableList value);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_extension_ListValue thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_extension_ListValue thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_extension_ListValue thiz);
          } ListValue;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_extension_LongValue_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_extension_LongValue_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_extension_LongValue_$serializer thiz);
              libgraphdatamodel_kref_model_extension_LongValue (*deserialize)(libgraphdatamodel_kref_model_extension_LongValue_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_extension_LongValue_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_extension_LongValue value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_extension_LongValue_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_extension_LongValue_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_extension_LongValue (*LongValue)(libgraphdatamodel_KLong value);
            libgraphdatamodel_KLong (*get_value)(libgraphdatamodel_kref_model_extension_LongValue thiz);
            libgraphdatamodel_KLong (*component1)(libgraphdatamodel_kref_model_extension_LongValue thiz);
            libgraphdatamodel_kref_model_extension_LongValue (*copy)(libgraphdatamodel_kref_model_extension_LongValue thiz, libgraphdatamodel_KLong value);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_extension_LongValue thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_extension_LongValue thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_extension_LongValue thiz);
          } LongValue;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_extension_MapValue_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_extension_MapValue_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_extension_MapValue_$serializer thiz);
              libgraphdatamodel_kref_model_extension_MapValue (*deserialize)(libgraphdatamodel_kref_model_extension_MapValue_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_extension_MapValue_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_extension_MapValue value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_extension_MapValue_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_extension_MapValue_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_extension_MapValue (*MapValue)(libgraphdatamodel_kref_kotlin_collections_MutableMap value);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*get_value)(libgraphdatamodel_kref_model_extension_MapValue thiz);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*component1)(libgraphdatamodel_kref_model_extension_MapValue thiz);
            libgraphdatamodel_kref_model_extension_MapValue (*copy)(libgraphdatamodel_kref_model_extension_MapValue thiz, libgraphdatamodel_kref_kotlin_collections_MutableMap value);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_extension_MapValue thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_extension_MapValue thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_extension_MapValue thiz);
          } MapValue;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_extension_StringValue_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_extension_StringValue_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_extension_StringValue_$serializer thiz);
              libgraphdatamodel_kref_model_extension_StringValue (*deserialize)(libgraphdatamodel_kref_model_extension_StringValue_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_extension_StringValue_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_extension_StringValue value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_extension_StringValue_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_extension_StringValue_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_extension_StringValue (*StringValue)(const char* value);
            const char* (*get_value)(libgraphdatamodel_kref_model_extension_StringValue thiz);
            const char* (*component1)(libgraphdatamodel_kref_model_extension_StringValue thiz);
            libgraphdatamodel_kref_model_extension_StringValue (*copy)(libgraphdatamodel_kref_model_extension_StringValue thiz, const char* value);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_extension_StringValue thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_extension_StringValue thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_extension_StringValue thiz);
          } StringValue;
        } extension;
        struct {
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_mapping_LabelMapping_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_mapping_LabelMapping_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_mapping_LabelMapping_$serializer thiz);
              libgraphdatamodel_kref_model_mapping_LabelMapping (*deserialize)(libgraphdatamodel_kref_model_mapping_LabelMapping_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_mapping_LabelMapping_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_mapping_LabelMapping value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_mapping_LabelMapping_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_mapping_LabelMapping_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_mapping_LabelMapping (*LabelMapping)(const char* table, const char* field);
            const char* (*get_field)(libgraphdatamodel_kref_model_mapping_LabelMapping thiz);
            const char* (*get_table)(libgraphdatamodel_kref_model_mapping_LabelMapping thiz);
            const char* (*component1)(libgraphdatamodel_kref_model_mapping_LabelMapping thiz);
            const char* (*component2)(libgraphdatamodel_kref_model_mapping_LabelMapping thiz);
            libgraphdatamodel_kref_model_mapping_LabelMapping (*copy)(libgraphdatamodel_kref_model_mapping_LabelMapping thiz, const char* table, const char* field);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_mapping_LabelMapping thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_mapping_LabelMapping thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_mapping_LabelMapping thiz);
          } LabelMapping;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_mapping_Mapping_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_mapping_Mapping_Companion thiz, libgraphdatamodel_kref_kotlin_Array typeParamsSerializers);
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer_)(libgraphdatamodel_kref_model_mapping_Mapping_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
          } Mapping;
          struct {
            struct {
              libgraphdatamodel_kref_model_mapping_MappingMode (*get)(); /* enum entry for MERGE. */
            } MERGE;
            struct {
              libgraphdatamodel_kref_model_mapping_MappingMode (*get)(); /* enum entry for CREATE. */
            } CREATE;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_mapping_MappingMode_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_mapping_MappingMode_Companion thiz, libgraphdatamodel_kref_kotlin_Array typeParamsSerializers);
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer_)(libgraphdatamodel_kref_model_mapping_MappingMode_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
          } MappingMode;
          struct {
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_mapping_MappingSerializer (*_instance)();
            libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_mapping_MappingSerializer thiz);
            libgraphdatamodel_kref_kotlinx_serialization_DeserializationStrategy (*selectDeserializer)(libgraphdatamodel_kref_model_mapping_MappingSerializer thiz, libgraphdatamodel_kref_kotlinx_serialization_json_JsonElement element);
          } MappingSerializer;
          struct {
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_mapping_MappingType (*_instance)();
            const char* (*get_LABEL)(libgraphdatamodel_kref_model_mapping_MappingType thiz);
            const char* (*get_NODE)(libgraphdatamodel_kref_model_mapping_MappingType thiz);
            const char* (*get_QUERY)(libgraphdatamodel_kref_model_mapping_MappingType thiz);
            const char* (*get_RELATIONSHIP)(libgraphdatamodel_kref_model_mapping_MappingType thiz);
          } MappingType;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_mapping_NodeMapping_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_mapping_NodeMapping_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_mapping_NodeMapping_$serializer thiz);
              libgraphdatamodel_kref_model_mapping_NodeMapping (*deserialize)(libgraphdatamodel_kref_model_mapping_NodeMapping_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_mapping_NodeMapping_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_mapping_NodeMapping value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_mapping_NodeMapping_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_mapping_NodeMapping_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_mapping_NodeMapping (*NodeMapping)(const char* node, const char* table, libgraphdatamodel_kref_kotlin_collections_Map properties, libgraphdatamodel_kref_model_mapping_MappingMode mode, const char* matchLabel, libgraphdatamodel_kref_kotlin_collections_Set keys);
            libgraphdatamodel_kref_kotlin_collections_Set (*get_keys)(libgraphdatamodel_kref_model_mapping_NodeMapping thiz);
            const char* (*get_matchLabel)(libgraphdatamodel_kref_model_mapping_NodeMapping thiz);
            libgraphdatamodel_kref_model_mapping_MappingMode (*get_mode)(libgraphdatamodel_kref_model_mapping_NodeMapping thiz);
            const char* (*get_node)(libgraphdatamodel_kref_model_mapping_NodeMapping thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*get_properties)(libgraphdatamodel_kref_model_mapping_NodeMapping thiz);
            const char* (*get_table)(libgraphdatamodel_kref_model_mapping_NodeMapping thiz);
            const char* (*component1)(libgraphdatamodel_kref_model_mapping_NodeMapping thiz);
            const char* (*component2)(libgraphdatamodel_kref_model_mapping_NodeMapping thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*component3)(libgraphdatamodel_kref_model_mapping_NodeMapping thiz);
            libgraphdatamodel_kref_model_mapping_MappingMode (*component4)(libgraphdatamodel_kref_model_mapping_NodeMapping thiz);
            const char* (*component5)(libgraphdatamodel_kref_model_mapping_NodeMapping thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*component6)(libgraphdatamodel_kref_model_mapping_NodeMapping thiz);
            libgraphdatamodel_kref_model_mapping_NodeMapping (*copy)(libgraphdatamodel_kref_model_mapping_NodeMapping thiz, const char* node, const char* table, libgraphdatamodel_kref_kotlin_collections_Map properties, libgraphdatamodel_kref_model_mapping_MappingMode mode, const char* matchLabel, libgraphdatamodel_kref_kotlin_collections_Set keys);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_mapping_NodeMapping thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_mapping_NodeMapping thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_mapping_NodeMapping thiz);
          } NodeMapping;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_mapping_PropertyMapping_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_mapping_PropertyMapping_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_mapping_PropertyMapping_$serializer thiz);
              libgraphdatamodel_kref_model_mapping_PropertyMapping (*deserialize)(libgraphdatamodel_kref_model_mapping_PropertyMapping_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_mapping_PropertyMapping_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_mapping_PropertyMapping value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_mapping_PropertyMapping_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_mapping_PropertyMapping_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_mapping_PropertyMapping (*PropertyMapping)(const char* field);
            const char* (*get_field)(libgraphdatamodel_kref_model_mapping_PropertyMapping thiz);
            const char* (*component1)(libgraphdatamodel_kref_model_mapping_PropertyMapping thiz);
            libgraphdatamodel_kref_model_mapping_PropertyMapping (*copy)(libgraphdatamodel_kref_model_mapping_PropertyMapping thiz, const char* field);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_mapping_PropertyMapping thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_mapping_PropertyMapping thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_mapping_PropertyMapping thiz);
          } PropertyMapping;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_mapping_QueryMapping_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_mapping_QueryMapping_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_mapping_QueryMapping_$serializer thiz);
              libgraphdatamodel_kref_model_mapping_QueryMapping (*deserialize)(libgraphdatamodel_kref_model_mapping_QueryMapping_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_mapping_QueryMapping_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_mapping_QueryMapping value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_mapping_QueryMapping_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_mapping_QueryMapping_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_mapping_QueryMapping (*QueryMapping)(const char* table, const char* query);
            const char* (*get_query)(libgraphdatamodel_kref_model_mapping_QueryMapping thiz);
            const char* (*get_table)(libgraphdatamodel_kref_model_mapping_QueryMapping thiz);
            const char* (*component1)(libgraphdatamodel_kref_model_mapping_QueryMapping thiz);
            const char* (*component2)(libgraphdatamodel_kref_model_mapping_QueryMapping thiz);
            libgraphdatamodel_kref_model_mapping_QueryMapping (*copy)(libgraphdatamodel_kref_model_mapping_QueryMapping thiz, const char* table, const char* query);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_mapping_QueryMapping thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_mapping_QueryMapping thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_mapping_QueryMapping thiz);
          } QueryMapping;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_mapping_RelationshipMapping_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_mapping_RelationshipMapping_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_mapping_RelationshipMapping_$serializer thiz);
              libgraphdatamodel_kref_model_mapping_RelationshipMapping (*deserialize)(libgraphdatamodel_kref_model_mapping_RelationshipMapping_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_mapping_RelationshipMapping_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_mapping_RelationshipMapping value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_mapping_RelationshipMapping_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_mapping_RelationshipMapping_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_mapping_RelationshipMapping (*RelationshipMapping)(const char* relationship, const char* table, libgraphdatamodel_kref_model_mapping_TargetMapping from, libgraphdatamodel_kref_model_mapping_TargetMapping to, const char* matchLabel, libgraphdatamodel_kref_kotlin_collections_Map properties, libgraphdatamodel_kref_model_mapping_MappingMode mode, libgraphdatamodel_kref_kotlin_collections_Set keys);
            libgraphdatamodel_kref_model_mapping_TargetMapping (*get_from)(libgraphdatamodel_kref_model_mapping_RelationshipMapping thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*get_keys)(libgraphdatamodel_kref_model_mapping_RelationshipMapping thiz);
            const char* (*get_matchLabel)(libgraphdatamodel_kref_model_mapping_RelationshipMapping thiz);
            libgraphdatamodel_kref_model_mapping_MappingMode (*get_mode)(libgraphdatamodel_kref_model_mapping_RelationshipMapping thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*get_properties)(libgraphdatamodel_kref_model_mapping_RelationshipMapping thiz);
            const char* (*get_relationship)(libgraphdatamodel_kref_model_mapping_RelationshipMapping thiz);
            const char* (*get_table)(libgraphdatamodel_kref_model_mapping_RelationshipMapping thiz);
            libgraphdatamodel_kref_model_mapping_TargetMapping (*get_to)(libgraphdatamodel_kref_model_mapping_RelationshipMapping thiz);
            const char* (*component1)(libgraphdatamodel_kref_model_mapping_RelationshipMapping thiz);
            const char* (*component2)(libgraphdatamodel_kref_model_mapping_RelationshipMapping thiz);
            libgraphdatamodel_kref_model_mapping_TargetMapping (*component3)(libgraphdatamodel_kref_model_mapping_RelationshipMapping thiz);
            libgraphdatamodel_kref_model_mapping_TargetMapping (*component4)(libgraphdatamodel_kref_model_mapping_RelationshipMapping thiz);
            const char* (*component5)(libgraphdatamodel_kref_model_mapping_RelationshipMapping thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*component6)(libgraphdatamodel_kref_model_mapping_RelationshipMapping thiz);
            libgraphdatamodel_kref_model_mapping_MappingMode (*component7)(libgraphdatamodel_kref_model_mapping_RelationshipMapping thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*component8)(libgraphdatamodel_kref_model_mapping_RelationshipMapping thiz);
            libgraphdatamodel_kref_model_mapping_RelationshipMapping (*copy)(libgraphdatamodel_kref_model_mapping_RelationshipMapping thiz, const char* relationship, const char* table, libgraphdatamodel_kref_model_mapping_TargetMapping from, libgraphdatamodel_kref_model_mapping_TargetMapping to, const char* matchLabel, libgraphdatamodel_kref_kotlin_collections_Map properties, libgraphdatamodel_kref_model_mapping_MappingMode mode, libgraphdatamodel_kref_kotlin_collections_Set keys);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_mapping_RelationshipMapping thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_mapping_RelationshipMapping thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_mapping_RelationshipMapping thiz);
          } RelationshipMapping;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_mapping_TargetMapping_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_mapping_TargetMapping_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_mapping_TargetMapping_$serializer thiz);
              libgraphdatamodel_kref_model_mapping_TargetMapping (*deserialize)(libgraphdatamodel_kref_model_mapping_TargetMapping_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_mapping_TargetMapping_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_mapping_TargetMapping value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_mapping_TargetMapping_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_mapping_TargetMapping_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_mapping_TargetMapping (*TargetMapping)(const char* node, const char* label, libgraphdatamodel_kref_kotlin_collections_Map properties);
            const char* (*get_label)(libgraphdatamodel_kref_model_mapping_TargetMapping thiz);
            const char* (*get_node)(libgraphdatamodel_kref_model_mapping_TargetMapping thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*get_properties)(libgraphdatamodel_kref_model_mapping_TargetMapping thiz);
            const char* (*component1)(libgraphdatamodel_kref_model_mapping_TargetMapping thiz);
            const char* (*component2)(libgraphdatamodel_kref_model_mapping_TargetMapping thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*component3)(libgraphdatamodel_kref_model_mapping_TargetMapping thiz);
            libgraphdatamodel_kref_model_mapping_TargetMapping (*copy)(libgraphdatamodel_kref_model_mapping_TargetMapping thiz, const char* node, const char* label, libgraphdatamodel_kref_kotlin_collections_Map properties);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_mapping_TargetMapping thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_mapping_TargetMapping thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_mapping_TargetMapping thiz);
          } TargetMapping;
        } mapping;
        struct {
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_node_Labels_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_node_Labels_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_node_Labels_$serializer thiz);
              libgraphdatamodel_kref_model_node_Labels (*deserialize)(libgraphdatamodel_kref_model_node_Labels_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_node_Labels_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_node_Labels value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_node_Labels_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_node_Labels_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_node_Labels (*Labels)(const char* identifier, libgraphdatamodel_kref_kotlin_collections_Set implied, libgraphdatamodel_kref_kotlin_collections_Set optional, libgraphdatamodel_kref_kotlin_collections_MutableMap extensions);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*get_extensions)(libgraphdatamodel_kref_model_node_Labels thiz);
            const char* (*get_identifier)(libgraphdatamodel_kref_model_node_Labels thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*get_implied)(libgraphdatamodel_kref_model_node_Labels thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*get_optional)(libgraphdatamodel_kref_model_node_Labels thiz);
            const char* (*component1)(libgraphdatamodel_kref_model_node_Labels thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*component2)(libgraphdatamodel_kref_model_node_Labels thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*component3)(libgraphdatamodel_kref_model_node_Labels thiz);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*component4)(libgraphdatamodel_kref_model_node_Labels thiz);
            libgraphdatamodel_kref_model_node_Labels (*copy)(libgraphdatamodel_kref_model_node_Labels thiz, const char* identifier, libgraphdatamodel_kref_kotlin_collections_Set implied, libgraphdatamodel_kref_kotlin_collections_Set optional, libgraphdatamodel_kref_kotlin_collections_MutableMap extensions);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_node_Labels thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_node_Labels thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_node_Labels thiz);
          } Labels;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_node_Node_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_node_Node_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_node_Node_$serializer thiz);
              libgraphdatamodel_kref_model_node_Node (*deserialize)(libgraphdatamodel_kref_model_node_Node_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_node_Node_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_node_Node value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_node_Node_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_node_Node_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_node_Node (*Node)(libgraphdatamodel_kref_model_node_Labels labels, libgraphdatamodel_kref_kotlin_collections_Map properties, libgraphdatamodel_kref_kotlin_collections_Map constraints, libgraphdatamodel_kref_kotlin_collections_Map indexes, libgraphdatamodel_kref_kotlin_collections_MutableMap extensions, const char* name);
            libgraphdatamodel_kref_kotlin_collections_Map (*get_constraints)(libgraphdatamodel_kref_model_node_Node thiz);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*get_extensions)(libgraphdatamodel_kref_model_node_Node thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*get_indexes)(libgraphdatamodel_kref_model_node_Node thiz);
            libgraphdatamodel_kref_model_node_Labels (*get_labels)(libgraphdatamodel_kref_model_node_Node thiz);
            const char* (*get_name)(libgraphdatamodel_kref_model_node_Node thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*get_properties)(libgraphdatamodel_kref_model_node_Node thiz);
            libgraphdatamodel_kref_model_node_Labels (*component1)(libgraphdatamodel_kref_model_node_Node thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*component2)(libgraphdatamodel_kref_model_node_Node thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*component3)(libgraphdatamodel_kref_model_node_Node thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*component4)(libgraphdatamodel_kref_model_node_Node thiz);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*component5)(libgraphdatamodel_kref_model_node_Node thiz);
            const char* (*component6)(libgraphdatamodel_kref_model_node_Node thiz);
            libgraphdatamodel_kref_model_node_Node (*copy)(libgraphdatamodel_kref_model_node_Node thiz, libgraphdatamodel_kref_model_node_Labels labels, libgraphdatamodel_kref_kotlin_collections_Map properties, libgraphdatamodel_kref_kotlin_collections_Map constraints, libgraphdatamodel_kref_kotlin_collections_Map indexes, libgraphdatamodel_kref_kotlin_collections_MutableMap extensions, const char* name);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_node_Node thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_node_Node thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_node_Node thiz);
          } Node;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_node_NodeConstraint_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_node_NodeConstraint_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_node_NodeConstraint_$serializer thiz);
              libgraphdatamodel_kref_model_node_NodeConstraint (*deserialize)(libgraphdatamodel_kref_model_node_NodeConstraint_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_node_NodeConstraint_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_node_NodeConstraint value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_node_NodeConstraint_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_node_NodeConstraint_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_node_NodeConstraint (*NodeConstraint)(const char* type, const char* label, libgraphdatamodel_kref_kotlin_collections_Set properties, libgraphdatamodel_kref_kotlin_collections_MutableMap extensions);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*get_extensions)(libgraphdatamodel_kref_model_node_NodeConstraint thiz);
            const char* (*get_label)(libgraphdatamodel_kref_model_node_NodeConstraint thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*get_properties)(libgraphdatamodel_kref_model_node_NodeConstraint thiz);
            const char* (*get_type)(libgraphdatamodel_kref_model_node_NodeConstraint thiz);
            const char* (*component1)(libgraphdatamodel_kref_model_node_NodeConstraint thiz);
            const char* (*component2)(libgraphdatamodel_kref_model_node_NodeConstraint thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*component3)(libgraphdatamodel_kref_model_node_NodeConstraint thiz);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*component4)(libgraphdatamodel_kref_model_node_NodeConstraint thiz);
            libgraphdatamodel_kref_model_node_NodeConstraint (*copy)(libgraphdatamodel_kref_model_node_NodeConstraint thiz, const char* type, const char* label, libgraphdatamodel_kref_kotlin_collections_Set properties, libgraphdatamodel_kref_kotlin_collections_MutableMap extensions);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_node_NodeConstraint thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_node_NodeConstraint thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_node_NodeConstraint thiz);
          } NodeConstraint;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_node_NodeIndex_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_node_NodeIndex_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_node_NodeIndex_$serializer thiz);
              libgraphdatamodel_kref_model_node_NodeIndex (*deserialize)(libgraphdatamodel_kref_model_node_NodeIndex_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_node_NodeIndex_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_node_NodeIndex value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_node_NodeIndex_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_node_NodeIndex_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_node_NodeIndex (*NodeIndex)(const char* type, libgraphdatamodel_kref_kotlin_collections_Set labels, libgraphdatamodel_kref_kotlin_collections_Set properties, libgraphdatamodel_kref_kotlin_collections_Map options, libgraphdatamodel_kref_kotlin_collections_MutableMap extensions);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*get_extensions)(libgraphdatamodel_kref_model_node_NodeIndex thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*get_labels)(libgraphdatamodel_kref_model_node_NodeIndex thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*get_options)(libgraphdatamodel_kref_model_node_NodeIndex thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*get_properties)(libgraphdatamodel_kref_model_node_NodeIndex thiz);
            const char* (*get_type)(libgraphdatamodel_kref_model_node_NodeIndex thiz);
            const char* (*component1)(libgraphdatamodel_kref_model_node_NodeIndex thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*component2)(libgraphdatamodel_kref_model_node_NodeIndex thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*component3)(libgraphdatamodel_kref_model_node_NodeIndex thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*component4)(libgraphdatamodel_kref_model_node_NodeIndex thiz);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*component5)(libgraphdatamodel_kref_model_node_NodeIndex thiz);
            libgraphdatamodel_kref_model_node_NodeIndex (*copy)(libgraphdatamodel_kref_model_node_NodeIndex thiz, const char* type, libgraphdatamodel_kref_kotlin_collections_Set labels, libgraphdatamodel_kref_kotlin_collections_Set properties, libgraphdatamodel_kref_kotlin_collections_Map options, libgraphdatamodel_kref_kotlin_collections_MutableMap extensions);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_node_NodeIndex thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_node_NodeIndex thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_node_NodeIndex thiz);
          } NodeIndex;
        } node;
        struct {
          struct {
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for ANY. */
            } ANY;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for BOOLEAN. */
            } BOOLEAN;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for LIST_BOOLEAN. */
            } LIST_BOOLEAN;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for DATE. */
            } DATE;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for LIST_DATE. */
            } LIST_DATE;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for DURATION. */
            } DURATION;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for LIST_DURATION. */
            } LIST_DURATION;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for FLOAT32. */
            } FLOAT32;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for LIST_FLOAT32. */
            } LIST_FLOAT32;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for FLOAT. */
            } FLOAT;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for LIST_FLOAT. */
            } LIST_FLOAT;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for INTEGER8. */
            } INTEGER8;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for LIST_INTEGER8. */
            } LIST_INTEGER8;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for INTEGER16. */
            } INTEGER16;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for LIST_INTEGER16. */
            } LIST_INTEGER16;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for INTEGER32. */
            } INTEGER32;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for LIST_INTEGER32. */
            } LIST_INTEGER32;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for INTEGER. */
            } INTEGER;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for LIST_INTEGER. */
            } LIST_INTEGER;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for LOCAL_DATETIME. */
            } LOCAL_DATETIME;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for LIST_LOCAL_DATETIME. */
            } LIST_LOCAL_DATETIME;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for LOCAL_TIME. */
            } LOCAL_TIME;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for LIST_LOCAL_TIME. */
            } LIST_LOCAL_TIME;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for POINT. */
            } POINT;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for LIST_POINT. */
            } LIST_POINT;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for STRING. */
            } STRING;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for LIST_STRING. */
            } LIST_STRING;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for VECTOR_FLOAT. */
            } VECTOR_FLOAT;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for VECTOR_FLOAT32. */
            } VECTOR_FLOAT32;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for VECTOR_INTEGER. */
            } VECTOR_INTEGER;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for VECTOR_INTEGER32. */
            } VECTOR_INTEGER32;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for VECTOR_INTEGER16. */
            } VECTOR_INTEGER16;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for VECTOR_INTEGER8. */
            } VECTOR_INTEGER8;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for ZONED_DATETIME. */
            } ZONED_DATETIME;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for LIST_ZONED_DATETIME. */
            } LIST_ZONED_DATETIME;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for ZONED_TIME. */
            } ZONED_TIME;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for LIST_ZONED_TIME. */
            } LIST_ZONED_TIME;
            struct {
              libgraphdatamodel_kref_model_property_Neo4jType (*get)(); /* enum entry for UUID. */
            } UUID;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_property_Neo4jType_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_property_Neo4jType_Companion thiz, libgraphdatamodel_kref_kotlin_Array typeParamsSerializers);
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer_)(libgraphdatamodel_kref_model_property_Neo4jType_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
          } Neo4jType;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_property_Property_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_property_Property_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_property_Property_$serializer thiz);
              libgraphdatamodel_kref_model_property_Property (*deserialize)(libgraphdatamodel_kref_model_property_Property_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_property_Property_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_property_Property value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_property_Property_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_property_Property_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_property_Property (*Property)(libgraphdatamodel_kref_model_property_Neo4jType type, libgraphdatamodel_KBoolean nullable, libgraphdatamodel_KBoolean unique, libgraphdatamodel_kref_kotlin_collections_MutableMap extensions, const char* name);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*get_extensions)(libgraphdatamodel_kref_model_property_Property thiz);
            libgraphdatamodel_KBoolean (*get_key)(libgraphdatamodel_kref_model_property_Property thiz);
            const char* (*get_name)(libgraphdatamodel_kref_model_property_Property thiz);
            libgraphdatamodel_KBoolean (*get_nullable)(libgraphdatamodel_kref_model_property_Property thiz);
            libgraphdatamodel_kref_model_property_Neo4jType (*get_type)(libgraphdatamodel_kref_model_property_Property thiz);
            libgraphdatamodel_KBoolean (*get_unique)(libgraphdatamodel_kref_model_property_Property thiz);
            libgraphdatamodel_kref_model_property_Neo4jType (*component1)(libgraphdatamodel_kref_model_property_Property thiz);
            libgraphdatamodel_KBoolean (*component2)(libgraphdatamodel_kref_model_property_Property thiz);
            libgraphdatamodel_KBoolean (*component3)(libgraphdatamodel_kref_model_property_Property thiz);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*component4)(libgraphdatamodel_kref_model_property_Property thiz);
            const char* (*component5)(libgraphdatamodel_kref_model_property_Property thiz);
            libgraphdatamodel_kref_model_property_Property (*copy)(libgraphdatamodel_kref_model_property_Property thiz, libgraphdatamodel_kref_model_property_Neo4jType type, libgraphdatamodel_KBoolean nullable, libgraphdatamodel_KBoolean unique, libgraphdatamodel_kref_kotlin_collections_MutableMap extensions, const char* name);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_property_Property thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_property_Property thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_property_Property thiz);
          } Property;
        } property;
        struct {
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_relationship_Relationship_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_relationship_Relationship_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_relationship_Relationship_$serializer thiz);
              libgraphdatamodel_kref_model_relationship_Relationship (*deserialize)(libgraphdatamodel_kref_model_relationship_Relationship_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_relationship_Relationship_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_relationship_Relationship value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_relationship_Relationship_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_relationship_Relationship_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_relationship_Relationship (*Relationship)(const char* type, libgraphdatamodel_kref_model_relationship_RelationshipTarget from, libgraphdatamodel_kref_model_relationship_RelationshipTarget to, libgraphdatamodel_kref_kotlin_collections_Map properties, libgraphdatamodel_kref_kotlin_collections_Map constraints, libgraphdatamodel_kref_kotlin_collections_Map indexes, libgraphdatamodel_kref_kotlin_collections_MutableMap extensions, const char* name);
            libgraphdatamodel_kref_kotlin_collections_Map (*get_constraints)(libgraphdatamodel_kref_model_relationship_Relationship thiz);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*get_extensions)(libgraphdatamodel_kref_model_relationship_Relationship thiz);
            libgraphdatamodel_kref_model_relationship_RelationshipTarget (*get_from)(libgraphdatamodel_kref_model_relationship_Relationship thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*get_indexes)(libgraphdatamodel_kref_model_relationship_Relationship thiz);
            const char* (*get_name)(libgraphdatamodel_kref_model_relationship_Relationship thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*get_properties)(libgraphdatamodel_kref_model_relationship_Relationship thiz);
            libgraphdatamodel_kref_model_relationship_RelationshipTarget (*get_to)(libgraphdatamodel_kref_model_relationship_Relationship thiz);
            const char* (*get_type)(libgraphdatamodel_kref_model_relationship_Relationship thiz);
            const char* (*component1)(libgraphdatamodel_kref_model_relationship_Relationship thiz);
            libgraphdatamodel_kref_model_relationship_RelationshipTarget (*component2)(libgraphdatamodel_kref_model_relationship_Relationship thiz);
            libgraphdatamodel_kref_model_relationship_RelationshipTarget (*component3)(libgraphdatamodel_kref_model_relationship_Relationship thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*component4)(libgraphdatamodel_kref_model_relationship_Relationship thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*component5)(libgraphdatamodel_kref_model_relationship_Relationship thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*component6)(libgraphdatamodel_kref_model_relationship_Relationship thiz);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*component7)(libgraphdatamodel_kref_model_relationship_Relationship thiz);
            const char* (*component8)(libgraphdatamodel_kref_model_relationship_Relationship thiz);
            libgraphdatamodel_kref_model_relationship_Relationship (*copy)(libgraphdatamodel_kref_model_relationship_Relationship thiz, const char* type, libgraphdatamodel_kref_model_relationship_RelationshipTarget from, libgraphdatamodel_kref_model_relationship_RelationshipTarget to, libgraphdatamodel_kref_kotlin_collections_Map properties, libgraphdatamodel_kref_kotlin_collections_Map constraints, libgraphdatamodel_kref_kotlin_collections_Map indexes, libgraphdatamodel_kref_kotlin_collections_MutableMap extensions, const char* name);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_relationship_Relationship thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_relationship_Relationship thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_relationship_Relationship thiz);
          } Relationship;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_relationship_RelationshipConstraint_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_relationship_RelationshipConstraint_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_relationship_RelationshipConstraint_$serializer thiz);
              libgraphdatamodel_kref_model_relationship_RelationshipConstraint (*deserialize)(libgraphdatamodel_kref_model_relationship_RelationshipConstraint_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_relationship_RelationshipConstraint_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_relationship_RelationshipConstraint value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_relationship_RelationshipConstraint_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_relationship_RelationshipConstraint_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_relationship_RelationshipConstraint (*RelationshipConstraint)(const char* type, libgraphdatamodel_kref_kotlin_collections_Set properties, libgraphdatamodel_kref_kotlin_collections_Map options, libgraphdatamodel_kref_kotlin_collections_MutableMap extensions);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*get_extensions)(libgraphdatamodel_kref_model_relationship_RelationshipConstraint thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*get_options)(libgraphdatamodel_kref_model_relationship_RelationshipConstraint thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*get_properties)(libgraphdatamodel_kref_model_relationship_RelationshipConstraint thiz);
            const char* (*get_type)(libgraphdatamodel_kref_model_relationship_RelationshipConstraint thiz);
            const char* (*component1)(libgraphdatamodel_kref_model_relationship_RelationshipConstraint thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*component2)(libgraphdatamodel_kref_model_relationship_RelationshipConstraint thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*component3)(libgraphdatamodel_kref_model_relationship_RelationshipConstraint thiz);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*component4)(libgraphdatamodel_kref_model_relationship_RelationshipConstraint thiz);
            libgraphdatamodel_kref_model_relationship_RelationshipConstraint (*copy)(libgraphdatamodel_kref_model_relationship_RelationshipConstraint thiz, const char* type, libgraphdatamodel_kref_kotlin_collections_Set properties, libgraphdatamodel_kref_kotlin_collections_Map options, libgraphdatamodel_kref_kotlin_collections_MutableMap extensions);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_relationship_RelationshipConstraint thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_relationship_RelationshipConstraint thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_relationship_RelationshipConstraint thiz);
          } RelationshipConstraint;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_relationship_RelationshipIndex_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_relationship_RelationshipIndex_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_relationship_RelationshipIndex_$serializer thiz);
              libgraphdatamodel_kref_model_relationship_RelationshipIndex (*deserialize)(libgraphdatamodel_kref_model_relationship_RelationshipIndex_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_relationship_RelationshipIndex_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_relationship_RelationshipIndex value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_relationship_RelationshipIndex_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_relationship_RelationshipIndex_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_relationship_RelationshipIndex (*RelationshipIndex)(const char* type, libgraphdatamodel_kref_kotlin_collections_Set properties, libgraphdatamodel_kref_kotlin_collections_Map options, libgraphdatamodel_kref_kotlin_collections_MutableMap extensions);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*get_extensions)(libgraphdatamodel_kref_model_relationship_RelationshipIndex thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*get_options)(libgraphdatamodel_kref_model_relationship_RelationshipIndex thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*get_properties)(libgraphdatamodel_kref_model_relationship_RelationshipIndex thiz);
            const char* (*get_type)(libgraphdatamodel_kref_model_relationship_RelationshipIndex thiz);
            const char* (*component1)(libgraphdatamodel_kref_model_relationship_RelationshipIndex thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*component2)(libgraphdatamodel_kref_model_relationship_RelationshipIndex thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*component3)(libgraphdatamodel_kref_model_relationship_RelationshipIndex thiz);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*component4)(libgraphdatamodel_kref_model_relationship_RelationshipIndex thiz);
            libgraphdatamodel_kref_model_relationship_RelationshipIndex (*copy)(libgraphdatamodel_kref_model_relationship_RelationshipIndex thiz, const char* type, libgraphdatamodel_kref_kotlin_collections_Set properties, libgraphdatamodel_kref_kotlin_collections_Map options, libgraphdatamodel_kref_kotlin_collections_MutableMap extensions);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_relationship_RelationshipIndex thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_relationship_RelationshipIndex thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_relationship_RelationshipIndex thiz);
          } RelationshipIndex;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_relationship_RelationshipTarget_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_relationship_RelationshipTarget_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_relationship_RelationshipTarget_$serializer thiz);
              libgraphdatamodel_kref_model_relationship_RelationshipTarget (*deserialize)(libgraphdatamodel_kref_model_relationship_RelationshipTarget_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_relationship_RelationshipTarget_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_relationship_RelationshipTarget value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_relationship_RelationshipTarget_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_relationship_RelationshipTarget_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_relationship_RelationshipTarget (*RelationshipTarget)(const char* node, const char* label, const char* property);
            const char* (*get_label)(libgraphdatamodel_kref_model_relationship_RelationshipTarget thiz);
            const char* (*get_node)(libgraphdatamodel_kref_model_relationship_RelationshipTarget thiz);
            const char* (*get_property)(libgraphdatamodel_kref_model_relationship_RelationshipTarget thiz);
            const char* (*component1)(libgraphdatamodel_kref_model_relationship_RelationshipTarget thiz);
            const char* (*component2)(libgraphdatamodel_kref_model_relationship_RelationshipTarget thiz);
            const char* (*component3)(libgraphdatamodel_kref_model_relationship_RelationshipTarget thiz);
            libgraphdatamodel_kref_model_relationship_RelationshipTarget (*copy)(libgraphdatamodel_kref_model_relationship_RelationshipTarget thiz, const char* node, const char* label, const char* property);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_relationship_RelationshipTarget thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_relationship_RelationshipTarget thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_relationship_RelationshipTarget thiz);
          } RelationshipTarget;
        } relationship;
        struct {
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_source_ForeignKey_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_source_ForeignKey_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_source_ForeignKey_$serializer thiz);
              libgraphdatamodel_kref_model_source_ForeignKey (*deserialize)(libgraphdatamodel_kref_model_source_ForeignKey_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_source_ForeignKey_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_source_ForeignKey value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_source_ForeignKey_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_source_ForeignKey_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_source_ForeignKey (*ForeignKey)(libgraphdatamodel_kref_kotlin_collections_Set fields, libgraphdatamodel_kref_model_source_ForeignKeyReference references, libgraphdatamodel_kref_kotlin_collections_MutableMap extensions);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*get_extensions)(libgraphdatamodel_kref_model_source_ForeignKey thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*get_fields)(libgraphdatamodel_kref_model_source_ForeignKey thiz);
            libgraphdatamodel_kref_model_source_ForeignKeyReference (*get_references)(libgraphdatamodel_kref_model_source_ForeignKey thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*component1)(libgraphdatamodel_kref_model_source_ForeignKey thiz);
            libgraphdatamodel_kref_model_source_ForeignKeyReference (*component2)(libgraphdatamodel_kref_model_source_ForeignKey thiz);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*component3)(libgraphdatamodel_kref_model_source_ForeignKey thiz);
            libgraphdatamodel_kref_model_source_ForeignKey (*copy)(libgraphdatamodel_kref_model_source_ForeignKey thiz, libgraphdatamodel_kref_kotlin_collections_Set fields, libgraphdatamodel_kref_model_source_ForeignKeyReference references, libgraphdatamodel_kref_kotlin_collections_MutableMap extensions);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_source_ForeignKey thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_source_ForeignKey thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_source_ForeignKey thiz);
          } ForeignKey;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_source_ForeignKeyReference_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_source_ForeignKeyReference_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_source_ForeignKeyReference_$serializer thiz);
              libgraphdatamodel_kref_model_source_ForeignKeyReference (*deserialize)(libgraphdatamodel_kref_model_source_ForeignKeyReference_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_source_ForeignKeyReference_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_source_ForeignKeyReference value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_source_ForeignKeyReference_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_source_ForeignKeyReference_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_source_ForeignKeyReference (*ForeignKeyReference)(const char* table, libgraphdatamodel_kref_kotlin_collections_Set fields, libgraphdatamodel_kref_kotlin_collections_MutableMap extensions);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*get_extensions)(libgraphdatamodel_kref_model_source_ForeignKeyReference thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*get_fields)(libgraphdatamodel_kref_model_source_ForeignKeyReference thiz);
            const char* (*get_table)(libgraphdatamodel_kref_model_source_ForeignKeyReference thiz);
            const char* (*component1)(libgraphdatamodel_kref_model_source_ForeignKeyReference thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*component2)(libgraphdatamodel_kref_model_source_ForeignKeyReference thiz);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*component3)(libgraphdatamodel_kref_model_source_ForeignKeyReference thiz);
            libgraphdatamodel_kref_model_source_ForeignKeyReference (*copy)(libgraphdatamodel_kref_model_source_ForeignKeyReference thiz, const char* table, libgraphdatamodel_kref_kotlin_collections_Set fields, libgraphdatamodel_kref_kotlin_collections_MutableMap extensions);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_source_ForeignKeyReference thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_source_ForeignKeyReference thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_source_ForeignKeyReference thiz);
          } ForeignKeyReference;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_source_Table_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_source_Table_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_source_Table_$serializer thiz);
              libgraphdatamodel_kref_model_source_Table (*deserialize)(libgraphdatamodel_kref_model_source_Table_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_source_Table_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_source_Table value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_source_Table_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_source_Table_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_source_Table (*Table)(const char* source, libgraphdatamodel_kref_kotlin_collections_Map fields, libgraphdatamodel_kref_kotlin_collections_Set primaryKeys, libgraphdatamodel_kref_kotlin_collections_Map foreignKeys, libgraphdatamodel_kref_kotlin_collections_MutableMap extensions);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*get_extensions)(libgraphdatamodel_kref_model_source_Table thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*get_fields)(libgraphdatamodel_kref_model_source_Table thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*get_foreignKeys)(libgraphdatamodel_kref_model_source_Table thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*get_primaryKeys)(libgraphdatamodel_kref_model_source_Table thiz);
            const char* (*get_source)(libgraphdatamodel_kref_model_source_Table thiz);
            const char* (*component1)(libgraphdatamodel_kref_model_source_Table thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*component2)(libgraphdatamodel_kref_model_source_Table thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*component3)(libgraphdatamodel_kref_model_source_Table thiz);
            libgraphdatamodel_kref_kotlin_collections_Map (*component4)(libgraphdatamodel_kref_model_source_Table thiz);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*component5)(libgraphdatamodel_kref_model_source_Table thiz);
            libgraphdatamodel_kref_model_source_Table (*copy)(libgraphdatamodel_kref_model_source_Table thiz, const char* source, libgraphdatamodel_kref_kotlin_collections_Map fields, libgraphdatamodel_kref_kotlin_collections_Set primaryKeys, libgraphdatamodel_kref_kotlin_collections_Map foreignKeys, libgraphdatamodel_kref_kotlin_collections_MutableMap extensions);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_source_Table thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_source_Table thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_source_Table thiz);
          } Table;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_source_TableField_$serializer (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_model_source_TableField_$serializer thiz);
              libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_model_source_TableField_$serializer thiz);
              libgraphdatamodel_kref_model_source_TableField (*deserialize)(libgraphdatamodel_kref_model_source_TableField_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
              void (*serialize)(libgraphdatamodel_kref_model_source_TableField_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_model_source_TableField value);
            } $serializer;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_model_source_TableField_Companion (*_instance)();
              libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_model_source_TableField_Companion thiz);
            } Companion;
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_model_source_TableField (*TableField)(const char* type, libgraphdatamodel_KInt size, libgraphdatamodel_kref_model_property_Neo4jType suggested, libgraphdatamodel_kref_kotlin_collections_Set supported, libgraphdatamodel_kref_kotlin_collections_MutableMap extensions, const char* name);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*get_extensions)(libgraphdatamodel_kref_model_source_TableField thiz);
            const char* (*get_name)(libgraphdatamodel_kref_model_source_TableField thiz);
            libgraphdatamodel_KInt (*get_size)(libgraphdatamodel_kref_model_source_TableField thiz);
            libgraphdatamodel_kref_model_property_Neo4jType (*get_suggested)(libgraphdatamodel_kref_model_source_TableField thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*get_supported)(libgraphdatamodel_kref_model_source_TableField thiz);
            const char* (*get_type)(libgraphdatamodel_kref_model_source_TableField thiz);
            const char* (*component1)(libgraphdatamodel_kref_model_source_TableField thiz);
            libgraphdatamodel_KInt (*component2)(libgraphdatamodel_kref_model_source_TableField thiz);
            libgraphdatamodel_kref_model_property_Neo4jType (*component3)(libgraphdatamodel_kref_model_source_TableField thiz);
            libgraphdatamodel_kref_kotlin_collections_Set (*component4)(libgraphdatamodel_kref_model_source_TableField thiz);
            libgraphdatamodel_kref_kotlin_collections_MutableMap (*component5)(libgraphdatamodel_kref_model_source_TableField thiz);
            const char* (*component6)(libgraphdatamodel_kref_model_source_TableField thiz);
            libgraphdatamodel_kref_model_source_TableField (*copy)(libgraphdatamodel_kref_model_source_TableField thiz, const char* type, libgraphdatamodel_KInt size, libgraphdatamodel_kref_model_property_Neo4jType suggested, libgraphdatamodel_kref_kotlin_collections_Set supported, libgraphdatamodel_kref_kotlin_collections_MutableMap extensions, const char* name);
            libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_model_source_TableField thiz, libgraphdatamodel_kref_kotlin_Any other);
            libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_model_source_TableField thiz);
            const char* (*toString)(libgraphdatamodel_kref_model_source_TableField thiz);
          } TableField;
        } source;
        struct {
          struct {
            struct {
              libgraphdatamodel_kref_model_type_ConstraintType (*get)(); /* enum entry for EXISTS. */
            } EXISTS;
            struct {
              libgraphdatamodel_kref_model_type_ConstraintType (*get)(); /* enum entry for KEY. */
            } KEY;
            struct {
              libgraphdatamodel_kref_model_type_ConstraintType (*get)(); /* enum entry for TYPE. */
            } TYPE;
            struct {
              libgraphdatamodel_kref_model_type_ConstraintType (*get)(); /* enum entry for UNIQUE. */
            } UNIQUE;
            libgraphdatamodel_KType* (*_type)(void);
          } ConstraintType;
          struct {
            struct {
              libgraphdatamodel_kref_model_type_IndexType (*get)(); /* enum entry for FULLTEXT. */
            } FULLTEXT;
            struct {
              libgraphdatamodel_kref_model_type_IndexType (*get)(); /* enum entry for POINT. */
            } POINT;
            struct {
              libgraphdatamodel_kref_model_type_IndexType (*get)(); /* enum entry for RANGE. */
            } RANGE;
            struct {
              libgraphdatamodel_kref_model_type_IndexType (*get)(); /* enum entry for TEXT. */
            } TEXT;
            struct {
              libgraphdatamodel_kref_model_type_IndexType (*get)(); /* enum entry for VECTOR. */
            } VECTOR;
            struct {
              libgraphdatamodel_kref_model_type_IndexType (*get)(); /* enum entry for LOOKUP. */
            } LOOKUP;
            libgraphdatamodel_KType* (*_type)(void);
          } IndexType;
        } type;
        struct {
          libgraphdatamodel_KType* (*_type)(void);
          libgraphdatamodel_kref_model_Type (*_instance)();
          const char* (*get_DATA_MODEL)(libgraphdatamodel_kref_model_Type thiz);
          const char* (*get_GRAPH_SPEC)(libgraphdatamodel_kref_model_Type thiz);
          const char* (*get_GRAPH_SPEC_PRETTY)(libgraphdatamodel_kref_model_Type thiz);
          const char* (*get_IMPORT_SPEC)(libgraphdatamodel_kref_model_Type thiz);
        } Type;
        struct {
          libgraphdatamodel_KType* (*_type)(void);
          libgraphdatamodel_kref_model_Version (*_instance)();
          const char* (*get_DATA_MODEL_V23)(libgraphdatamodel_kref_model_Version thiz);
          const char* (*get_DATA_MODEL_V24)(libgraphdatamodel_kref_model_Version thiz);
          const char* (*get_DATA_MODEL_V30)(libgraphdatamodel_kref_model_Version thiz);
          const char* (*get_IMPORT_SPEC_V1)(libgraphdatamodel_kref_model_Version thiz);
          const char* (*get_LATEST)(libgraphdatamodel_kref_model_Version thiz);
        } Version;
      } model;
      struct {
        struct {
          struct {
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_validate_Issue_$serializer (*_instance)();
            libgraphdatamodel_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libgraphdatamodel_kref_validate_Issue_$serializer thiz);
            libgraphdatamodel_kref_kotlin_Array (*childSerializers)(libgraphdatamodel_kref_validate_Issue_$serializer thiz);
            libgraphdatamodel_kref_validate_Issue (*deserialize)(libgraphdatamodel_kref_validate_Issue_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Decoder decoder);
            void (*serialize)(libgraphdatamodel_kref_validate_Issue_$serializer thiz, libgraphdatamodel_kref_kotlinx_serialization_encoding_Encoder encoder, libgraphdatamodel_kref_validate_Issue value);
          } $serializer;
          struct {
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_validate_Issue_Companion (*_instance)();
            libgraphdatamodel_kref_kotlinx_serialization_KSerializer (*serializer)(libgraphdatamodel_kref_validate_Issue_Companion thiz);
          } Companion;
          libgraphdatamodel_KType* (*_type)(void);
          libgraphdatamodel_kref_validate_Issue (*Issue)(const char* code, const char* message, const char* path, libgraphdatamodel_kref_kotlin_collections_Map details);
          const char* (*get_code)(libgraphdatamodel_kref_validate_Issue thiz);
          libgraphdatamodel_kref_kotlin_collections_Map (*get_details)(libgraphdatamodel_kref_validate_Issue thiz);
          const char* (*get_message)(libgraphdatamodel_kref_validate_Issue thiz);
          const char* (*get_path)(libgraphdatamodel_kref_validate_Issue thiz);
          const char* (*component1)(libgraphdatamodel_kref_validate_Issue thiz);
          const char* (*component2)(libgraphdatamodel_kref_validate_Issue thiz);
          const char* (*component3)(libgraphdatamodel_kref_validate_Issue thiz);
          libgraphdatamodel_kref_kotlin_collections_Map (*component4)(libgraphdatamodel_kref_validate_Issue thiz);
          libgraphdatamodel_kref_validate_Issue (*copy)(libgraphdatamodel_kref_validate_Issue thiz, const char* code, const char* message, const char* path, libgraphdatamodel_kref_kotlin_collections_Map details);
          libgraphdatamodel_KBoolean (*equals)(libgraphdatamodel_kref_validate_Issue thiz, libgraphdatamodel_kref_kotlin_Any other);
          libgraphdatamodel_KInt (*hashCode)(libgraphdatamodel_kref_validate_Issue thiz);
          const char* (*toString)(libgraphdatamodel_kref_validate_Issue thiz);
        } Issue;
        struct {
          struct {
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_validate_node_NodeConstraints (*_instance)();
            void (*validateConstraint)(libgraphdatamodel_kref_validate_node_NodeConstraints thiz, libgraphdatamodel_kref_model_GraphModel model, const char* nodeId, libgraphdatamodel_kref_model_node_Node node, const char* constraintId, libgraphdatamodel_kref_model_node_NodeConstraint constraint, libgraphdatamodel_kref_kotlin_collections_MutableList issues);
          } NodeConstraints;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_validate_node_constraint_NodeExistenceConstraint (*_instance)();
              libgraphdatamodel_kref_kotlin_collections_List (*dependsOn)(libgraphdatamodel_kref_validate_node_constraint_NodeExistenceConstraint thiz);
              void (*validateConstraint)(libgraphdatamodel_kref_validate_node_constraint_NodeExistenceConstraint thiz, libgraphdatamodel_kref_model_GraphModel model, const char* nodeId, libgraphdatamodel_kref_model_node_Node node, const char* constraintId, libgraphdatamodel_kref_model_node_NodeConstraint constraint, libgraphdatamodel_kref_kotlin_collections_MutableList issues);
            } NodeExistenceConstraint;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_validate_node_constraint_NodeTypeConstraint (*_instance)();
              libgraphdatamodel_kref_kotlin_collections_List (*dependsOn)(libgraphdatamodel_kref_validate_node_constraint_NodeTypeConstraint thiz);
              void (*validateConstraint)(libgraphdatamodel_kref_validate_node_constraint_NodeTypeConstraint thiz, libgraphdatamodel_kref_model_GraphModel model, const char* nodeId, libgraphdatamodel_kref_model_node_Node node, const char* constraintId, libgraphdatamodel_kref_model_node_NodeConstraint constraint, libgraphdatamodel_kref_kotlin_collections_MutableList issues);
            } NodeTypeConstraint;
          } constraint;
          struct {
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_validate_node_NodeIndexesExists (*_instance)();
            void (*validateIndex)(libgraphdatamodel_kref_validate_node_NodeIndexesExists thiz, libgraphdatamodel_kref_model_GraphModel model, const char* nodeId, libgraphdatamodel_kref_model_node_Node node, const char* indexId, libgraphdatamodel_kref_model_node_NodeIndex index, libgraphdatamodel_kref_kotlin_collections_MutableList issues);
          } NodeIndexesExists;
          struct {
            libgraphdatamodel_KType* (*_type)(void);
            void (*validate)(libgraphdatamodel_kref_validate_node_NodeValidation thiz, libgraphdatamodel_kref_model_GraphModel model, libgraphdatamodel_kref_kotlin_collections_MutableList issues);
            void (*validateConstraint)(libgraphdatamodel_kref_validate_node_NodeValidation thiz, libgraphdatamodel_kref_model_GraphModel model, const char* nodeId, libgraphdatamodel_kref_model_node_Node node, const char* constraintId, libgraphdatamodel_kref_model_node_NodeConstraint constraint, libgraphdatamodel_kref_kotlin_collections_MutableList issues);
            void (*validateIndex)(libgraphdatamodel_kref_validate_node_NodeValidation thiz, libgraphdatamodel_kref_model_GraphModel model, const char* nodeId, libgraphdatamodel_kref_model_node_Node node, const char* indexId, libgraphdatamodel_kref_model_node_NodeIndex index, libgraphdatamodel_kref_kotlin_collections_MutableList issues);
            void (*validateNode)(libgraphdatamodel_kref_validate_node_NodeValidation thiz, libgraphdatamodel_kref_model_GraphModel model, const char* nodeId, libgraphdatamodel_kref_model_node_Node node, libgraphdatamodel_kref_kotlin_collections_MutableList issues);
            void (*validateProperty)(libgraphdatamodel_kref_validate_node_NodeValidation thiz, libgraphdatamodel_kref_model_GraphModel model, const char* nodeId, libgraphdatamodel_kref_model_node_Node node, const char* propertyId, libgraphdatamodel_kref_model_property_Property property, libgraphdatamodel_kref_kotlin_collections_MutableList issues);
          } NodeValidation;
        } node;
        struct {
          struct {
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_validate_relationship_RelationshipConstraints (*_instance)();
            void (*validateConstraint)(libgraphdatamodel_kref_validate_relationship_RelationshipConstraints thiz, libgraphdatamodel_kref_model_GraphModel model, const char* relationshipId, libgraphdatamodel_kref_model_relationship_Relationship relationship, const char* constraintId, libgraphdatamodel_kref_model_relationship_RelationshipConstraint constraint, libgraphdatamodel_kref_kotlin_collections_MutableList issues);
          } RelationshipConstraints;
          struct {
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_validate_relationship_constraint_RelationshipExistenceConstraint (*_instance)();
              libgraphdatamodel_kref_kotlin_collections_List (*dependsOn)(libgraphdatamodel_kref_validate_relationship_constraint_RelationshipExistenceConstraint thiz);
              void (*validateConstraint)(libgraphdatamodel_kref_validate_relationship_constraint_RelationshipExistenceConstraint thiz, libgraphdatamodel_kref_model_GraphModel model, const char* relationshipId, libgraphdatamodel_kref_model_relationship_Relationship relationship, const char* constraintId, libgraphdatamodel_kref_model_relationship_RelationshipConstraint constraint, libgraphdatamodel_kref_kotlin_collections_MutableList issues);
            } RelationshipExistenceConstraint;
            struct {
              libgraphdatamodel_KType* (*_type)(void);
              libgraphdatamodel_kref_validate_relationship_constraint_RelationshipTypeConstraint (*_instance)();
              libgraphdatamodel_kref_kotlin_collections_List (*dependsOn)(libgraphdatamodel_kref_validate_relationship_constraint_RelationshipTypeConstraint thiz);
              void (*validateConstraint)(libgraphdatamodel_kref_validate_relationship_constraint_RelationshipTypeConstraint thiz, libgraphdatamodel_kref_model_GraphModel model, const char* relationshipId, libgraphdatamodel_kref_model_relationship_Relationship relationship, const char* constraintId, libgraphdatamodel_kref_model_relationship_RelationshipConstraint constraint, libgraphdatamodel_kref_kotlin_collections_MutableList issues);
            } RelationshipTypeConstraint;
          } constraint;
          struct {
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_validate_relationship_RelationshipIndexes (*_instance)();
            void (*validateIndex)(libgraphdatamodel_kref_validate_relationship_RelationshipIndexes thiz, libgraphdatamodel_kref_model_GraphModel model, const char* relationshipId, libgraphdatamodel_kref_model_relationship_Relationship relationship, const char* indexId, libgraphdatamodel_kref_model_relationship_RelationshipIndex index, libgraphdatamodel_kref_kotlin_collections_MutableList issues);
          } RelationshipIndexes;
          struct {
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_validate_relationship_RelationshipNodes (*_instance)();
            void (*validateRelationship)(libgraphdatamodel_kref_validate_relationship_RelationshipNodes thiz, libgraphdatamodel_kref_model_GraphModel model, const char* relationshipId, libgraphdatamodel_kref_model_relationship_Relationship relationship, libgraphdatamodel_kref_kotlin_collections_MutableList issues);
          } RelationshipNodes;
          struct {
            libgraphdatamodel_KType* (*_type)(void);
            void (*validate)(libgraphdatamodel_kref_validate_relationship_RelationshipValidation thiz, libgraphdatamodel_kref_model_GraphModel model, libgraphdatamodel_kref_kotlin_collections_MutableList issues);
            void (*validateConstraint)(libgraphdatamodel_kref_validate_relationship_RelationshipValidation thiz, libgraphdatamodel_kref_model_GraphModel model, const char* relationshipId, libgraphdatamodel_kref_model_relationship_Relationship relationship, const char* constraintId, libgraphdatamodel_kref_model_relationship_RelationshipConstraint constraint, libgraphdatamodel_kref_kotlin_collections_MutableList issues);
            void (*validateIndex)(libgraphdatamodel_kref_validate_relationship_RelationshipValidation thiz, libgraphdatamodel_kref_model_GraphModel model, const char* relationshipId, libgraphdatamodel_kref_model_relationship_Relationship relationship, const char* indexId, libgraphdatamodel_kref_model_relationship_RelationshipIndex index, libgraphdatamodel_kref_kotlin_collections_MutableList issues);
            void (*validateProperty)(libgraphdatamodel_kref_validate_relationship_RelationshipValidation thiz, libgraphdatamodel_kref_model_GraphModel model, const char* relationshipId, libgraphdatamodel_kref_model_relationship_Relationship relationship, const char* propertyId, libgraphdatamodel_kref_model_property_Property property, libgraphdatamodel_kref_kotlin_collections_MutableList issues);
            void (*validateRelationship)(libgraphdatamodel_kref_validate_relationship_RelationshipValidation thiz, libgraphdatamodel_kref_model_GraphModel model, const char* relationshipId, libgraphdatamodel_kref_model_relationship_Relationship relationship, libgraphdatamodel_kref_kotlin_collections_MutableList issues);
          } RelationshipValidation;
        } relationship;
        struct {
          libgraphdatamodel_KType* (*_type)(void);
          libgraphdatamodel_kref_kotlin_collections_List (*dependsOn)(libgraphdatamodel_kref_validate_Validation thiz);
          void (*validate)(libgraphdatamodel_kref_validate_Validation thiz, libgraphdatamodel_kref_model_GraphModel model, libgraphdatamodel_kref_kotlin_collections_MutableList issues);
        } Validation;
        struct {
          libgraphdatamodel_KType* (*_type)(void);
          libgraphdatamodel_kref_validate_ValidationTree (*ValidationTree)();
          void (*build)(libgraphdatamodel_kref_validate_ValidationTree thiz, libgraphdatamodel_kref_kotlin_collections_List validators);
          void (*clear)(libgraphdatamodel_kref_validate_ValidationTree thiz);
          libgraphdatamodel_kref_kotlin_collections_List (*validate)(libgraphdatamodel_kref_validate_ValidationTree thiz, libgraphdatamodel_kref_model_GraphModel model);
        } ValidationTree;
        struct {
          struct {
            libgraphdatamodel_KType* (*_type)(void);
            libgraphdatamodel_kref_validate_Validations_Companion (*_instance)();
            libgraphdatamodel_kref_kotlin_collections_List (*get_all)(libgraphdatamodel_kref_validate_Validations_Companion thiz);
          } Companion;
          libgraphdatamodel_KType* (*_type)(void);
          libgraphdatamodel_kref_validate_Validations (*Validations)();
        } Validations;
      } validate;
      struct {
        struct {
          libgraphdatamodel_KType* (*_type)(void);
          libgraphdatamodel_kref_GraphSpecConfig_Builder (*Builder)(libgraphdatamodel_kref_GraphSpecConfig config);
          libgraphdatamodel_kref_GraphSpecConfig_Builder (*Builder_)(libgraphdatamodel_kref_codec_format_Format format);
          libgraphdatamodel_kref_codec_format_Format (*get_format)(libgraphdatamodel_kref_GraphSpecConfig_Builder thiz);
          void (*set_format)(libgraphdatamodel_kref_GraphSpecConfig_Builder thiz, libgraphdatamodel_kref_codec_format_Format set);
          libgraphdatamodel_kref_kotlin_collections_MutableMap (*get_migrations)(libgraphdatamodel_kref_GraphSpecConfig_Builder thiz);
          libgraphdatamodel_kref_kotlin_collections_MutableList (*get_validators)(libgraphdatamodel_kref_GraphSpecConfig_Builder thiz);
          libgraphdatamodel_kref_GraphSpecConfig (*build)(libgraphdatamodel_kref_GraphSpecConfig_Builder thiz);
          void (*format)(libgraphdatamodel_kref_GraphSpecConfig_Builder thiz, libgraphdatamodel_kref_codec_format_Format format);
          void (*json)(libgraphdatamodel_kref_GraphSpecConfig_Builder thiz, libgraphdatamodel_kref_kotlin_Function1 builder);
          void (*migrate)(libgraphdatamodel_kref_GraphSpecConfig_Builder thiz, libgraphdatamodel_kref_migrate_Migration migration);
          void (*validate)(libgraphdatamodel_kref_GraphSpecConfig_Builder thiz, libgraphdatamodel_kref_kotlin_Array validation);
        } Builder;
        libgraphdatamodel_KType* (*_type)(void);
        libgraphdatamodel_kref_GraphSpecConfig (*GraphSpecConfig)(libgraphdatamodel_kref_kotlin_collections_List validators, libgraphdatamodel_kref_kotlin_collections_Map migrations, libgraphdatamodel_kref_codec_format_Format format);
        libgraphdatamodel_kref_codec_format_Format (*get_format)(libgraphdatamodel_kref_GraphSpecConfig thiz);
        libgraphdatamodel_kref_kotlin_collections_Map (*get_migrations)(libgraphdatamodel_kref_GraphSpecConfig thiz);
        libgraphdatamodel_kref_kotlin_collections_List (*get_validators)(libgraphdatamodel_kref_GraphSpecConfig thiz);
      } GraphSpecConfig;
      libgraphdatamodel_kref_GraphSpec (*GraphSpec_)(libgraphdatamodel_kref_GraphSpec from, libgraphdatamodel_kref_kotlin_Function1 builderAction);
    } root;
  } kotlin;
} libgraphdatamodel_ExportedSymbols;
extern libgraphdatamodel_ExportedSymbols* libgraphdatamodel_symbols(void);
#ifdef __cplusplus
}  /* extern "C" */
#endif
#endif  /* KONAN_LIBGRAPHDATAMODEL_H */
