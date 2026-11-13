# 🔀 GitHub — commit, push, PR-ийн бүрэн заавар

Энэ баримт нь LOS төслийн өөрчлөлтийг GitHub руу хэрхэн аюулгүй оруулахыг
алхам алхмаар тайлбарлана. Репо: `https://github.com/Tsendjav/loan-origination-system`

---

## 0. Урьдчилсан шалгалт

```bash
git --version                     # 2.30+
git config --get user.name        # хоосон бол 1-р алхмыг үзнэ
git config --get user.email
git remote -v                     # origin зөв эсэх
git status                        # юу өөрчлөгдсөн
```

## 1. Git identity (нэг удаа)

```bash
# Зөвхөн энэ репод:
git config --local user.name "Таны Нэр"
git config --local user.email "tanii@email.mn"

# Бүх репод (сонголтоор):
git config --global user.name "Таны Нэр"
git config --global user.email "tanii@email.mn"
```

## 2. Нэвтрэлт (нэг удаа)

**Хувийн access token (HTTPS) — хамгийн хялбар:**

1. GitHub → Settings → Developer settings → Personal access tokens → **Fine-grained tokens** → Generate new token
2. Repository access: `Tsendjav/loan-origination-system`
3. Permissions: **Contents: Read and write**, **Pull requests: Read and write**
4. `git push` хийх үед username = GitHub нэр, password = **token** (жинхэнэ нууц үг биш)

Токеныг хадгалах:

```bash
git config --global credential.helper store   # нээлттэй текстээр хадгална (зөвхөн хувийн компьютер)
```

**SSH (илүү аюулгүй):**

```bash
ssh-keygen -t ed25519 -C "tanii@email.mn"
cat ~/.ssh/id_ed25519.pub        # GitHub → Settings → SSH and GPG keys → New SSH key
ssh -T git@github.com            # "Hi <user>! You've successfully authenticated" гарах ёстой
git remote set-url origin git@github.com:Tsendjav/loan-origination-system.git
```

## 3. Өөрчлөлтийг commit хийх

```bash
git status                       # юу өөрчлөгдсөн
git diff                         # агуулгыг харах (commit хийхээс өмнө!)
git add <файл>                   # эсвэл: git add -A  (бүгд)
git diff --cached                # stage хийснийг дахин харах
```

Commit мессеж (жишээ):

```bash
git commit -m "$(cat <<'EOF'
Зээлийн автомат шийдвэрийн дүрмийг нэмэв

- Дүн ≤10 сая₮, хугацаа ≤60 сар, оноо ≥650 бол APPROVED
- Бусад тохиолдолд UNDER_REVIEW
EOF
)"
```

Сайн мессежийн дүрэм: эхний мөр ≤72 тэмдэгт, **юу** биш **яагаад**-ыг бичнэ; нэг commit = нэг логик өөрчлөлт.

> Том файл, `node_modules/`, `target/`, `dist/`, `.env`, лог, screenshot-ыг commit хийхгүй —
> `.gitignore`-д байгаа эсэхийг шалгана.

## 4. Салбар үүсгэж push хийх (санал болгож буй урсгал)

```bash
git checkout -b feature/credit-scoring      # main-аас шинэ салбар
# ... өөрчлөлт, commit ...
git push -u origin feature/credit-scoring
```

`-u` нь дараагийн удаа зүгээр `git push` хийхэд холбоно.

## 5. PR (Pull Request) нээх

Push хийсний дараа терминал дээр гарч ирэх холбоосыг нээх, эсвэл:

```
https://github.com/Tsendjav/loan-origination-system/pull/new/<салбар>
```

PR-д:
1. Гарчиг: юу өөрчлөгдсөн (жишээ: `Автомат шийдвэрийн дүрэм`)
2. Тайлбар: юу, яагаад, хэрхэн шалгасан
3. Reviewer сонгох
4. **Checks** ногоон болсныг хүлээх (`LOS CI`, `Docker Build & Stack Verification`)

## 6. Merge

- **Squash and merge** — олон жижиг commit-ыг нэг болгож `main` руу оруулна (санал болгож буй).
- **Rebase and merge** — commit бүр хадгалагдана.
- Merge хийсний дараа локал салбараа цэвэрлэнэ:

```bash
git checkout main
git pull origin main
git branch -d feature/credit-scoring
```

## 7. Conflict шийдэх

```bash
git checkout main
git pull origin main
git checkout feature/credit-scoring
git merge main                 # эсвэл: git rebase main
# <<<<<<< / ======= / >>>>>>> тэмдэглэгээтэй файлуудыг засна
git add <зассан файлууд>
git commit                     # merge үед
# rebase үед: git rebase --continue
git push
```

Rebase хийсний дараа push хийхэд `--force-with-lease` шаардлагатай болно
(`--force` биш — бусдын commit-ыг устгахаас хамгаална):

```bash
git push --force-with-lease
```

## 8. Буцаах (rollback)

```bash
# Сүүлийн commit-ыг хадгалаад буцаах (шинэ commit үүснэ):
git revert <commit-hash>
git push

# Commit хийгээгүй өөрчлөлтийг хаях:
git restore <файл>
git restore .                  # бүгд

# Локал commit-ыг буцаах (push хийгээгүй бол):
git reset --soft HEAD~1        # өөрчлөлт хэвээр, commit устана
```

> `main` дээр `git reset --hard` + `push --force` хийхгүй — бусдын ажлыг устгана.

## 9. CI/CD-тэй холбоо

| Workflow | Хэзээ | Юу хийдэг |
|---|---|---|
| `LOS CI` (`ci.yml`) | push/PR | Backend `mvn verify`, frontend typecheck+lint+build, compose config |
| `Docker Build & Stack Verification` | push/PR | Образ build + compose stack + smoke |
| `Deploy` (`deploy.yml`) | **зөвхөн гараар** | GHCR push → SSH deploy → health + read-only smoke |

Deploy хийх: Actions → Deploy → Run workflow → `environment`, `image_tag`, `build_images`.

## 10. Түгээмэл алдаа

| Алдаа | Шалтгаан / шийдэл |
|---|---|
| `Author identity unknown` | `git config --local user.name/user.email` тохируулна |
| `Permission denied (publickey)` | SSH key GitHub-д бүртгэгдээгүй, эсвэл `ssh -T git@github.com` шалгана |
| `Authentication failed` (HTTPS) | Нууц үг биш **token** ашиглана |
| `failed to push some refs ... non-fast-forward` | `git pull --rebase origin main`, conflict шийдээд push |
| `Updates were rejected because the remote contains work` | Эхлээд `git pull`, дараа нь push |
| `GH001: Large files detected` | 100MB-аас том файлыг `git lfs` эсвэл `.gitignore` |
| `refusing to merge unrelated histories` | `git pull origin main --allow-unrelated-histories` |
| Checks улаан | Actions log-оос алдааг хараад локал дээр мөн командыг ажиллуулна |

## 11. Энэ төслийн одоогийн төлөв (жишээ)

```bash
# Одоогийн салбар ба commit
git log --oneline -3
git status

# Remote руу push (салбар үүснэ)
git push -u origin feature/credit-scoring

# main-тай харьцуулах
git log --oneline origin/main..HEAD
git diff --stat origin/main...HEAD
```
