## DeadChest - Troubleshooting

This page covers common issues and quick fixes.

### Ghost/leftover hologram

Use:

```text
/dc repair
```

If needed, use the force variant:

```text
/dc repair force
```

You need admin permission (`deadchest.admin`) for repair commands.

### DeadChest not behaving as expected after update

1. Check your server version and Java version match plugin requirements.
2. Run `/dc reload` after config changes.
3. Verify `config.yml` was migrated correctly to schema version `2`.
4. Check startup logs for localization/config parsing warnings.

!!! note
Avoid deleting plugin data unless you know the impact on active chests.

### Commands not working

- Ensure command is `/dc ...`
- Verify required permissions for your user/group
- Check `permissions.require-*` settings in config

### WorldGuard behavior is incorrect

- Ensure `integrations.worldguard.enabled: true`
- Verify region flags (`dc-owner`, `dc-member`, `dc-guest`)
- Check `integrations.worldguard.default-allow`

### Need more help

- Share startup logs and relevant config sections when asking for support.
- Join Discord support from the home page links.

