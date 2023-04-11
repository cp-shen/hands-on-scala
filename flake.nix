{
  inputs.nixpkgs.url = "github:nixos/nixpkgs";
  inputs.flake-utils.url = "github:numtide/flake-utils";

  outputs = { nixpkgs, flake-utils, ... }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };

        jdkToUse = pkgs.jdk17;

        scalaJvmDeps = with pkgs; [
          jdkToUse
          (sbt.override { jre = jdkToUse; })
          (mill.override { jre = jdkToUse; })
          (scala-cli.override { jre = jdkToUse; })
          #(coursier.override { jre = jdkToUse; })
          (metals.override { jre = jdkToUse; })
          (scalafmt.override { jre = jdkToUse; })
        ];
        scalaJsDeps = with pkgs; [
          nodejs
          #yarn
          #electron
        ];
        scalaNativeDeps = with pkgs; [
          zlib
          clang
          llvmPackages.libcxxabi
        ];
        shellHook = ''
          export JAVA_HOME="${jdkToUse.home}";
        '';
      in rec {
        devShells.scalaWithNativeAndJs = pkgs.mkShell {
          inherit shellHook;
          packages = scalaJvmDeps ++ scalaJsDeps ++ scalaNativeDeps;
        };
        devShells.scalaWithNative = pkgs.mkShell {
          inherit shellHook;
          packages = scalaJvmDeps ++ scalaNativeDeps;
        };
        devShells.scalaWithJs = pkgs.mkShell {
          inherit shellHook;
          packages = scalaJvmDeps ++ scalaJsDeps;
        };
        devShells.scala = pkgs.mkShell {
          inherit shellHook;
          packages = scalaJvmDeps;
        };
        devShells.default = devShells.scalaWithNativeAndJs;
      });
}
