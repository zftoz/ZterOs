import struct

def parse_webp(path):
    with open(path, 'rb') as f:
        data = f.read()
    print(path, "len=", len(data), data[:12])
    # check VP8 or VP8L or VP8X
    pos = 12
    while pos < len(data):
        chunk_fourcc = data[pos:pos+4]
        chunk_size = struct.unpack('<I', data[pos+4:pos+8])[0]
        print(f"  Chunk {chunk_fourcc} size={chunk_size}")
        pos += 8 + chunk_size + (chunk_size % 2)

parse_webp('app/src/main/res/drawable/system_browser.png')
parse_webp('app/src/main/res/drawable/system_camera.png')
