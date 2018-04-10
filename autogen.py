#!/usr/bin/env python

# Copyright 2018, Robert Dyer,
#                 and Bowling Green State University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""A protoc plugin to generate Java code for the Boa compiler.

This plugin will read the .proto file and translate any concrete and shadow
types into their Boa compiler representations."""

__author__ = 'Robert Dyer <rdyer@bgsu.edu>'

import sys
from string import Template

from google.protobuf import descriptor
from google.protobuf.descriptor_pool import DescriptorPool
from google.protobuf.compiler import plugin_pb2 as plugin

from hidden_pb2 import alias, hidden, kind_func

paths = []

types_pkg = 'boa.types'

def root():
    """return the root directory"""

    return '/'.join(paths[0].split('.'))

def record_path(path):
    """tracks nested names"""

    paths.append(javaname(path))

def get_deps(request, pool):
    """generate the filenames of any imported file so we can ignore them in
    the analysis"""

    for proto_file in request.proto_file:
        pool.Add(proto_file)
        for i in proto_file.dependency:
            yield i

def is_hidden_field(f):
    """determine if a field is marked as hidden
    
    this is based on it declaring an extension option '(hidden)'"""

    if f.GetOptions().HasExtension(hidden):
        return True

    return False

def get_field_alias(f):
    """if a field has an alias, return it
    
    this is based on it declaring an extension option '(alias)'"""

    if f.GetOptions().HasExtension(alias):
        return f.GetOptions().Extensions[alias]

    return None

def get_shadow_kind_onemany(nt):
    """determine if a message type is shadowing another type and returns the list of kinds

    this is based on it declaring fields 'shadow_kind_N'"""

    if not nt.fields_by_name.has_key('shadow_kind_1'):
        return None

    i = 1
    kinds = []
    while nt.fields_by_name.get('shadow_kind_' + str(i)):
        kinds.append(nt.fields_by_name.get('shadow_kind_' + str(i)))
        i = i + 1
    return kinds

def get_shadow_kind(nt):
    """determine if a message type is shadowing another type and returns the kind

    this is based on it declaring a field 'shadow_kind'"""

    if nt.fields_by_name.has_key('shadow_kind'):
        return nt.fields_by_name.get('shadow_kind')

    return None

def javaname(n):
    """converts a message/enum name into a Java class name"""

    s = ''
    capital = True
    for i in range(0, len(n)):
        if capital:
            s = s + n[i].upper()
            capital = False
        elif n[i] == '_':
            capital = True
        else:
            s = s + n[i]
    return s

def fqn(t):
    """returns the fully qualified Java name for a given message"""

    try:
        return '.'.join(paths[0:paths.index(t)]) + '.' + javaname(t)
    except:
        return '.'.join(paths) + '.' + javaname(t)

def get_qualified_type(f):
    """converts a protobuf type into a Boa compiler type"""

    if f.type == descriptor.FieldDescriptor.TYPE_MESSAGE:
        fieldtype = 'new ' + paths[0] + '.proto.' + f.message_type.name + 'ProtoTuple()'
    elif f.type == descriptor.FieldDescriptor.TYPE_GROUP:
        fieldtype = 'new ' + paths[0] + '.proto.' + f.message_type.name + 'ProtoTuple()'
    elif f.type == descriptor.FieldDescriptor.TYPE_ENUM:
        fieldtype = 'new ' + paths[0] + '.proto.enums.' + f.enum_type.name + 'ProtoMap()'
    elif f.type == descriptor.FieldDescriptor.TYPE_DOUBLE:
        fieldtype = 'new ' + types_pkg + '.BoaFloat()'
    elif f.type == descriptor.FieldDescriptor.TYPE_FLOAT:
        fieldtype = 'new ' + types_pkg + '.BoaFloat()'
    elif f.type == descriptor.FieldDescriptor.TYPE_INT64:
        fieldtype = 'new ' + types_pkg + '.BoaInt()'
    elif f.type == descriptor.FieldDescriptor.TYPE_UINT64:
        fieldtype = 'new ' + types_pkg + '.BoaInt()'
    elif f.type == descriptor.FieldDescriptor.TYPE_INT32:
        fieldtype = 'new ' + types_pkg + '.BoaInt()'
    elif f.type == descriptor.FieldDescriptor.TYPE_FIXED64:
        fieldtype = 'new ' + types_pkg + '.BoaInt()'
    elif f.type == descriptor.FieldDescriptor.TYPE_FIXED32:
        fieldtype = 'new ' + types_pkg + '.BoaInt()'
    elif f.type == descriptor.FieldDescriptor.TYPE_BOOL:
        fieldtype = 'new ' + types_pkg + '.BoaBool()'
    elif f.type == descriptor.FieldDescriptor.TYPE_STRING:
        fieldtype = 'new ' + types_pkg + '.BoaString()'
    elif f.type == descriptor.FieldDescriptor.TYPE_BYTES:
        fieldtype = 'new ' + types_pkg + '.BoaString()'
    elif f.type == descriptor.FieldDescriptor.TYPE_UINT32:
        fieldtype = 'new ' + types_pkg + '.BoaInt()'
    elif f.type == descriptor.FieldDescriptor.TYPE_SFIXED32:
        fieldtype = 'new ' + types_pkg + '.BoaInt()'
    elif f.type == descriptor.FieldDescriptor.TYPE_SFIXED64:
        fieldtype = 'new ' + types_pkg + '.BoaInt()'
    elif f.type == descriptor.FieldDescriptor.TYPE_SINT32:
        fieldtype = 'new ' + types_pkg + '.BoaInt()'
    elif f.type == descriptor.FieldDescriptor.TYPE_SINT64:
        fieldtype = 'new ' + types_pkg + '.BoaInt()'

    if f.label == descriptor.FieldDescriptor.LABEL_REPEATED:
        fieldtype = 'new ' + types_pkg + '.BoaProtoList(' + fieldtype + ')'

    return fieldtype

def get_shadowedtype(f):
    """converts a field's type name into what the shadowed field name would be"""

    if f.type == descriptor.FieldDescriptor.TYPE_MESSAGE:
        typename = f.message_type.name.split('.')[-1]
    elif f.type == descriptor.FieldDescriptor.TYPE_DOUBLE:
        typename = 'double'
    elif f.type == descriptor.FieldDescriptor.TYPE_FLOAT:
        typename = 'float'
    elif f.type == descriptor.FieldDescriptor.TYPE_INT64:
        typename = 'int64'
    elif f.type == descriptor.FieldDescriptor.TYPE_UINT64:
        typename = 'uint64'
    elif f.type == descriptor.FieldDescriptor.TYPE_INT32:
        typename = 'int32'
    elif f.type == descriptor.FieldDescriptor.TYPE_FIXED64:
        typename = 'fixed64'
    elif f.type == descriptor.FieldDescriptor.TYPE_FIXED32:
        typename = 'fixed32'
    elif f.type == descriptor.FieldDescriptor.TYPE_BOOL:
        typename = 'bool'
    elif f.type == descriptor.FieldDescriptor.TYPE_STRING:
        typename = 'string'
    elif f.type == descriptor.FieldDescriptor.TYPE_GROUP:
        typename = f.message_type.name.split('.')[-1]
    elif f.type == descriptor.FieldDescriptor.TYPE_BYTES:
        typename = 'bytes'
    elif f.type == descriptor.FieldDescriptor.TYPE_UINT32:
        typename = 'uint32'
    elif f.type == descriptor.FieldDescriptor.TYPE_ENUM:
        typename = f.enum_type.name.split('.')[-1]
    elif f.type == descriptor.FieldDescriptor.TYPE_SFIXED32:
        typename = 'sfixed32'
    elif f.type == descriptor.FieldDescriptor.TYPE_SFIXED64:
        typename = 'sfixed64'
    elif f.type == descriptor.FieldDescriptor.TYPE_SINT32:
        typename = 'sint32'
    elif f.type == descriptor.FieldDescriptor.TYPE_SINT64:
        typename = 'sint64'

    return typename

def generate_enum(response, proto, desc, enum):
    """generates the .java source code for an enum type"""

    print >> sys.stderr, "generating enum: " + enum.name + ' (' + root() + '/proto/enums/' + javaname(enum.name) + 'ProtoMap.java' + ')'

    with open('templates/enum.template') as f:
        s = Template(f.read()).substitute(
                pkg=paths[0],
                pkg2=types_pkg,
                name=enum.name,
                fqname=fqn(enum.name))

    f = response.file.add()
    f.name = root() + '/proto/enums/' + javaname(enum.name) + 'ProtoMap.java'
    f.content = s

def generate_concrete_class(response, t, proto):
    """generates the .java source code for a given concrete type"""

    print >> sys.stderr, "generating concrete class: " + t.name + ' (' + root() + '/proto/' + javaname(t.name) + 'ProtoTuple.java' + ')'

    fields = ''

    for f in t.fields:
        if not is_hidden_field(f):
            fields += '\n        names.put("' + f.name + '", count++);\n'
            fields += '        members.add(' + get_qualified_type(f) + ');\n'

    with open('templates/concrete-type.template') as f:
        s = Template(f.read()).substitute(
                pkg=paths[0],
                pkg2=types_pkg,
                name=t.name,
                fqname=fqn(t.name),
                fields=fields)

    f = response.file.add()
    f.name = root() + '/proto/' + javaname(t.name) + 'ProtoTuple.java'
    f.content = s

def generate_shadow_class(response, t, shadow, concrete_type):
    """generates the .java source code for a given shadow type"""

    print >> sys.stderr, "generating shadow class: " + t.name + ' (' + root() + '/shadow/' + javaname(t.name) + 'Shadow.java' + ')'

    fields = ''
    fieldstranslate = ''
    flattenfields = ''
    typemap = {}

    for f in t.fields:
        if f.name.startswith('shadow_kind'):
            continue

        typename = get_shadowedtype(f).lower()
        fieldtype = get_qualified_type(f)
        if f.label == descriptor.FieldDescriptor.LABEL_REPEATED:
            typename = typename + 's'

        # build code for the constructor
        fields += '        addShadow("' + f.name + '", ' + fieldtype + ');\n'

        # build code for the lookupCodegen method
        if typename not in typemap:
            typemap[typename] = 1

        alias = get_field_alias(f)
        if alias is None:
            concretefieldname = typename + '_' + str(typemap[typename])
        else:
            concretefieldname = alias
        fieldstranslate += '        if ("' + f.name + '".equals(name)) '
        if f.label == descriptor.FieldDescriptor.LABEL_REPEATED:
            fieldstranslate += 'return ASTFactory.createSelector("' + concretefieldname + '", new ' + types_pkg + '.BoaProtoList(' + fieldtype + '), env);\n'
        else:
            fieldstranslate += 'return ASTFactory.createSelector("' + concretefieldname + '", ' + fieldtype + ', env);\n'

        # build code for the flatten method
        flattenfields += '        '
        if f.label == descriptor.FieldDescriptor.LABEL_REPEATED:
            flattenfields += 'for (int i = 0; i < m.get' + javaname(f.name) + 'Count(); i++) '
            flattenfields += 'b.add' + javaname(concretefieldname) + '(m.get' + javaname(f.name) + '(i));\n'
        else:
            if f.label == descriptor.FieldDescriptor.LABEL_OPTIONAL:
                flattenfields += 'if (m.has' + javaname(f.name) + '()) '
            flattenfields += 'b.set' + javaname(concretefieldname) + '(m.get' + javaname(f.name) + '());\n'

        typemap[typename] = typemap[typename] + 1

    manyone = 'null'
    if t.GetOptions().HasExtension(kind_func):
        manyone = 'getManytoOne(env, b, "' + t.GetOptions().Extensions[kind_func] + '", new ' + paths[0] + '.proto.' + concrete_type.name + 'ProtoTuple())' 

    onemany = ''
    if isinstance(shadow, list):
        for s in shadow:
            onemany = onemany + '        l.add(getKindExpression("' + s.enum_type.name + '", "' + s.enum_type.values[s.default_value].name + '", new ' + types_pkg + '.proto.enums.' + s.enum_type.name + 'ProtoMap(), env));\n'
        shadow = shadow[0]

    with open('templates/shadow-type.template') as f:
        s = Template(f.read()).substitute(
                pkg=paths[0],
                pkg2=types_pkg,
                name=t.name,
                shadow=concrete_type.name,
                kindlongname=fqn(concrete_type.name) + '.' + shadow.enum_type.name,
                kindname=shadow.enum_type.name,
                enum=shadow.enum_type.values[shadow.default_value].name,
                fields=fields,
                fieldstranslate=fieldstranslate,
                manyone=manyone,
                onemany=onemany,
                proto=fqn(t.name),
                parentproto=fqn(concrete_type.name),
                flattenfields=flattenfields)

    f = response.file.add()
    f.name = root() + '/shadow/' + javaname(t.name) + 'Shadow.java'
    f.content = s

def process_message(response, proto, desc, m, concrete_type):
    """recursively processes a message type and all its contained types/enums,
    generating .java files as needed for both concrete and shadow types"""

    shadow = get_shadow_kind_onemany(m)
    if shadow is None:
        shadow = get_shadow_kind(m)
        if not shadow is None:
            generate_shadow_class(response, m, shadow, concrete_type)
        else:
            concrete_type = m
            generate_concrete_class(response, m, proto)
    else:
        generate_shadow_class(response, m, shadow, concrete_type)

    record_path(m.name)

    for nt in m.nested_types:
        process_message(response, proto, desc, nt, concrete_type)

    for enum in m.enum_types:
        generate_enum(response, proto, desc, enum)

    paths.pop()

if __name__ == '__main__':
    request = plugin.CodeGeneratorRequest()
    request.ParseFromString(sys.stdin.read())

    response = plugin.CodeGeneratorResponse()

    pool = DescriptorPool()
    deps = list(get_deps(request, pool))

    for proto in request.proto_file:
        if proto.name in deps:
            continue

        print >> sys.stderr, 'Processing ' + proto.name

        desc = pool.FindFileByName(proto.name)

        paths.append(desc.package)
        record_path(desc.name.split('.')[0])

        for message in desc.message_types_by_name:
            process_message(response, proto, desc, desc.message_types_by_name[message], desc.message_types_by_name[message])

        # FIXME does not generate top-level enums TypeKind and ChangeKind
        #for enum in desc.enum_types_by_name:
        #    generate_enum(response, proto, desc, enum)

        del paths[:]

    sys.stdout.write(response.SerializeToString())
