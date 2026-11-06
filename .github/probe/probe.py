"""Temporary API probe run on GitHub Actions (the dev sandbox has no internet)."""
import json, urllib.request, urllib.parse, urllib.error

UA = {"User-Agent": "frostkl4/noctis-launcher-api-probe (github.com/frostkl4/Launcher-minecraft-1-Mobile)"}

def get(url, params=None, raw=False, head=False):
    if params:
        url += "?" + urllib.parse.urlencode(params)
    req = urllib.request.Request(url, headers=UA, method="HEAD" if head else "GET")
    try:
        with urllib.request.urlopen(req, timeout=20) as r:
            body = b"" if head else r.read()
            return r.status, r.geturl(), dict(r.headers), body
    except urllib.error.HTTPError as e:
        return e.code, url, dict(e.headers), e.read()
    except Exception as e:
        return None, url, {}, str(e).encode()

def modrinth(facets, offset=0, limit=1, query=""):
    s, _, _, b = get("https://api.modrinth.com/v2/search",
                     {"facets": json.dumps(facets), "limit": limit, "offset": offset, "query": query})
    try:
        return s, json.loads(b)
    except Exception:
        return s, b[:200]

print("== MODRINTH facets (total_hits) ==")
for t in ["mod", "modpack", "resourcepack", "shader", "datapack", "plugin"]:
    out = []
    for f in ([["project_type:" + t]], [["all_project_types:" + t]],
              [["project_type:" + t, "all_project_types:" + t]]):
        s, d = modrinth(f)
        out.append(d.get("total_hits") if isinstance(d, dict) else f"ERR {s} {d}")
    print(t, "project_type=", out[0], "all_project_types=", out[1], "OR=", out[2])

print("== MODRINTH: hits in all_project_types:mod but not project_type:mod (sample) ==")
s, d = modrinth([["all_project_types:mod"], ["project_type!=mod"]], limit=8)
print("status", s, "total", d.get("total_hits") if isinstance(d, dict) else d)
if isinstance(d, dict):
    for h in d["hits"]:
        print("  ", h["slug"], "project_type=", h["project_type"], "all=", h.get("all_project_types"), "dl=", h["downloads"])

print("== MODRINTH: what does project_type:mod contain? (all_project_types sample) ==")
s, d = modrinth([["project_type:mod"], ["all_project_types!=mod"]], limit=8)
print("status", s, "total", d.get("total_hits") if isinstance(d, dict) else d)
if isinstance(d, dict):
    for h in d["hits"]:
        print("  ", h["slug"], "project_type=", h["project_type"], "all=", h.get("all_project_types"))

print("== MODRINTH deep offset ==")
for off in [0, 900, 1000, 5000, 10000, 20000, 50000]:
    s, d = modrinth([["project_type:mod"]], offset=off, limit=100)
    print(off, s, (len(d["hits"]), d.get("total_hits")) if isinstance(d, dict) else d)

print("== CURSEFORGE without key ==")
s, u, h, b = get("https://api.curseforge.com/v1/mods/search", {"gameId": 432, "classId": 6, "pageSize": 1})
print(s, b[:200])

print("== ELY.BY ==")
for u in ["https://skinsystem.ely.by/textures/erickskrauch",
          "https://skinsystem.ely.by/cloaks/erickskrauch.png",
          "http://skinsystem.ely.by/cloaks/erickskrauch.png",
          "https://skinsystem.ely.by/cloaks/notch.png",
          "https://skinsystem.ely.by/textures/nonexistent_user_zz9"]:
    s, final, h, b = get(u)
    print(u, "->", s, final, h.get("Content-Type"), len(b), b[:160] if b[:1] in (b"{", b"[") else "")

print("== MOJANG name -> uuid ==")
for u in ["https://api.mojang.com/users/profiles/minecraft/Notch",
          "https://api.minecraftservices.com/minecraft/profile/lookup/name/Notch"]:
    s, final, h, b = get(u)
    print(u, "->", s, b[:120])

print("== MINECRAFTCAPES ==")
for u in ["https://api.minecraftcapes.net/profile/069a79f444e94726a5befca90e38aaf5",
          "https://api.minecraftcapes.net/profile/069a79f444e94726a5befca90e38aaf5/cape",
          "https://api.minecraftcapes.net/profile/1d7a3cd29e0c4c3e9b5e3c9b1f0fa3a7",
          "https://minecraftcapes.net/profile/069a79f444e94726a5befca90e38aaf5/cape"]:
    s, final, h, b = get(u)
    body = b[:300]
    try:
        j = json.loads(b)
        if isinstance(j, dict) and "textures" in j:
            body = {k: (v if not isinstance(v, dict) else {kk: (str(vv)[:30] if vv else vv) for kk, vv in v.items()}) for k, v in j.items()}
    except Exception:
        pass
    print(u, "->", s, h.get("Content-Type"), body)
