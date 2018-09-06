# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: UploadModel.proto

import sys
_b=sys.version_info[0]<3 and (lambda x:x) or (lambda x:x.encode('latin1'))
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()




DESCRIPTOR = _descriptor.FileDescriptor(
  name='UploadModel.proto',
  package='NAS.Model',
  syntax='proto3',
  serialized_options=None,
  serialized_pb=_b('\n\x11UploadModel.proto\x12\tNAS.Model\"\xa2\x01\n\rStreamingNode\x12\n\n\x02id\x18\x01 \x01(\x05\x12\x0e\n\x06length\x18\x02 \x01(\x03\x12\x0e\n\x06stream\x18\x03 \x01(\x0c\x12\x14\n\x0cupload_modle\x18\x06 \x01(\t\x12&\n\x04left\x18\x04 \x01(\x0b\x32\x18.NAS.Model.StreamingNode\x12\'\n\x05right\x18\x05 \x01(\x0b\x32\x18.NAS.Model.StreamingNode\"_\n\x0bUploadModel\x12\n\n\x02id\x18\x01 \x01(\t\x12\x0c\n\x04name\x18\x02 \x01(\t\x12\x0e\n\x06length\x18\x03 \x01(\x03\x12&\n\x04root\x18\x04 \x01(\x0b\x32\x18.NAS.Model.StreamingNodeb\x06proto3')
)




_STREAMINGNODE = _descriptor.Descriptor(
  name='StreamingNode',
  full_name='NAS.Model.StreamingNode',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='id', full_name='NAS.Model.StreamingNode.id', index=0,
      number=1, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='length', full_name='NAS.Model.StreamingNode.length', index=1,
      number=2, type=3, cpp_type=2, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='stream', full_name='NAS.Model.StreamingNode.stream', index=2,
      number=3, type=12, cpp_type=9, label=1,
      has_default_value=False, default_value=_b(""),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='upload_modle', full_name='NAS.Model.StreamingNode.upload_modle', index=3,
      number=6, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='left', full_name='NAS.Model.StreamingNode.left', index=4,
      number=4, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='right', full_name='NAS.Model.StreamingNode.right', index=5,
      number=5, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=33,
  serialized_end=195,
)


_UPLOADMODEL = _descriptor.Descriptor(
  name='UploadModel',
  full_name='NAS.Model.UploadModel',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='id', full_name='NAS.Model.UploadModel.id', index=0,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='name', full_name='NAS.Model.UploadModel.name', index=1,
      number=2, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='length', full_name='NAS.Model.UploadModel.length', index=2,
      number=3, type=3, cpp_type=2, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='root', full_name='NAS.Model.UploadModel.root', index=3,
      number=4, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=197,
  serialized_end=292,
)

_STREAMINGNODE.fields_by_name['left'].message_type = _STREAMINGNODE
_STREAMINGNODE.fields_by_name['right'].message_type = _STREAMINGNODE
_UPLOADMODEL.fields_by_name['root'].message_type = _STREAMINGNODE
DESCRIPTOR.message_types_by_name['StreamingNode'] = _STREAMINGNODE
DESCRIPTOR.message_types_by_name['UploadModel'] = _UPLOADMODEL
_sym_db.RegisterFileDescriptor(DESCRIPTOR)

StreamingNode = _reflection.GeneratedProtocolMessageType('StreamingNode', (_message.Message,), dict(
  DESCRIPTOR = _STREAMINGNODE,
  __module__ = 'UploadModel_pb2'
  # @@protoc_insertion_point(class_scope:NAS.Model.StreamingNode)
  ))
_sym_db.RegisterMessage(StreamingNode)

UploadModel = _reflection.GeneratedProtocolMessageType('UploadModel', (_message.Message,), dict(
  DESCRIPTOR = _UPLOADMODEL,
  __module__ = 'UploadModel_pb2'
  # @@protoc_insertion_point(class_scope:NAS.Model.UploadModel)
  ))
_sym_db.RegisterMessage(UploadModel)


# @@protoc_insertion_point(module_scope)